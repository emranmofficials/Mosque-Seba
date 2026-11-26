import 'package:esc_pos_utils_plus/esc_pos_utils_plus.dart';
import 'package:flutter/foundation.dart';
import 'package:image/image.dart' as img;
import 'package:print_bluetooth_thermal/print_bluetooth_thermal.dart';
import 'package:printing/printing.dart';

/// Bluetooth ESC/POS Thermal Receipt & Slip Printing Service (58mm / 80mm).
class ThermalPrinterService {
  static final ThermalPrinterService instance = ThermalPrinterService();

  Future<bool> hasPermissionAndEnabled() async {
    try {
      final permitted = await PrintBluetoothThermal.isPermissionBluetoothGranted;
      if (!permitted) return false;
      return await PrintBluetoothThermal.bluetoothEnabled;
    } catch (e) {
      debugPrint('Bluetooth check error: $e');
      return false;
    }
  }

  Future<List<BluetoothInfo>> getPairedPrinters() async {
    try {
      return await PrintBluetoothThermal.pairedBluetooths;
    } catch (e) {
      debugPrint('Paired printers fetch error: $e');
      return [];
    }
  }

  Future<bool> get isConnected async {
    try {
      return await PrintBluetoothThermal.connectionStatus;
    } catch (_) {
      return false;
    }
  }

  Future<bool> connect(String macAddress) async {
    try {
      return await PrintBluetoothThermal.connect(
        macPrinterAddress: macAddress,
      );
    } catch (e) {
      debugPrint('Thermal printer connect error: $e');
      return false;
    }
  }

  Future<bool> disconnect() async {
    try {
      return await PrintBluetoothThermal.disconnect;
    } catch (_) {
      return false;
    }
  }

  /// Rasterizes generated Bengali PDF pages to monochrome bitmaps and prints
  /// over ESC/POS so complex Bengali script prints cleanly on 58mm/80mm rolls.
  Future<bool> printPdfReceipt({
    required Uint8List pdfBytes,
    PaperSize paperSize = PaperSize.mm58,
  }) async {
    try {
      final connected = await isConnected;
      if (!connected) return false;

      final profile = await CapabilityProfile.load();
      final generator = Generator(paperSize, profile);
      final escBytes = <int>[];

      await for (final page in Printing.raster(pdfBytes, dpi: 150)) {
        final pngBytes = await page.toPng();
        final decoded = img.decodePng(pngBytes);
        if (decoded == null) continue;
        final resized = img.copyResize(decoded, width: paperSize.width);
        escBytes.addAll(generator.imageRaster(resized));
        escBytes.addAll(generator.feed(2));
      }

      if (escBytes.isEmpty) return false;
      escBytes.addAll(generator.cut());
      return await PrintBluetoothThermal.writeBytes(escBytes);
    } catch (e) {
      debugPrint('Thermal raster print error: $e');
      return false;
    }
  }

  /// Sends a short ASCII printer connection test slip.
  Future<bool> printTestSlip({
    PaperSize paperSize = PaperSize.mm58,
  }) async {
    try {
      final connected = await isConnected;
      if (!connected) return false;

      final profile = await CapabilityProfile.load();
      final generator = Generator(paperSize, profile);
      final bytes = <int>[];
      bytes.addAll(
        generator.text(
          'MOSQUE SEBA',
          styles: const PosStyles(
            align: PosAlign.center,
            bold: true,
            width: PosTextSize.size2,
            height: PosTextSize.size2,
          ),
        ),
      );
      bytes.addAll(
        generator.text(
          'Bluetooth Thermal Test OK',
          styles: const PosStyles(align: PosAlign.center),
        ),
      );
      bytes.addAll(generator.hr());
      bytes.addAll(generator.feed(2));
      bytes.addAll(generator.cut());
      return await PrintBluetoothThermal.writeBytes(bytes);
    } catch (e) {
      debugPrint('Thermal test print error: $e');
      return false;
    }
  }
}
