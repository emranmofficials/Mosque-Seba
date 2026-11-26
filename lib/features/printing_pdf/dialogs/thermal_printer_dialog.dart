import 'dart:typed_data';
import 'package:esc_pos_utils_plus/esc_pos_utils_plus.dart';
import 'package:flutter/material.dart';
import 'package:print_bluetooth_thermal/print_bluetooth_thermal.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../services/thermal_printer_service.dart';

/// Opens a reusable Bluetooth Thermal Printer picker & print confirmation dialog.
Future<void> showThermalPrinterDialog(
  BuildContext context, {
  required Future<Uint8List> Function() buildPdf,
  String title = 'ব্লুটুথ থার্মাল প্রিন্টার (৫৮/৮০ মিমি)',
}) {
  return showDialog<void>(
    context: context,
    builder: (ctx) => _ThermalPrinterDialog(
      buildPdf: buildPdf,
      title: title,
    ),
  );
}

class _ThermalPrinterDialog extends StatefulWidget {
  final Future<Uint8List> Function() buildPdf;
  final String title;

  const _ThermalPrinterDialog({
    required this.buildPdf,
    required this.title,
  });

  @override
  State<_ThermalPrinterDialog> createState() => _ThermalPrinterDialogState();
}

class _ThermalPrinterDialogState extends State<_ThermalPrinterDialog> {
  final _service = ThermalPrinterService.instance;
  bool _loading = true;
  bool _printing = false;
  bool _btReady = false;
  bool _connected = false;
  String? _selectedMac;
  PaperSize _paperSize = PaperSize.mm58;
  List<BluetoothInfo> _devices = const [];
  String? _statusMessage;

  @override
  void initState() {
    super.initState();
    _scanDevices();
  }

  Future<void> _scanDevices() async {
    setState(() {
      _loading = true;
      _statusMessage = null;
    });
    final ready = await _service.hasPermissionAndEnabled();
    final connected = await _service.isConnected;
    final devices = ready ? await _service.getPairedPrinters() : <BluetoothInfo>[];
    if (!mounted) return;
    setState(() {
      _btReady = ready;
      _connected = connected;
      _devices = devices;
      _loading = false;
      if (!ready) {
        _statusMessage =
            'ব্লুটুথ চালু করুন এবং Nearby Devices অনুমতি দিন।';
      } else if (devices.isEmpty) {
        _statusMessage =
            'কোনো পেয়ার করা থার্মাল প্রিন্টার পাওয়া যায়নি। ফোনের Bluetooth সেটিংসে প্রিন্টার পেয়ার করুন।';
      }
    });
  }

  Future<void> _connectAndPrint(BluetoothInfo device) async {
    setState(() {
      _printing = true;
      _selectedMac = device.macAdress;
      _statusMessage = 'সংযোগ স্থাপন হচ্ছে: ${device.name}...';
    });

    final ok = await _service.connect(device.macAdress);
    if (!mounted) return;

    if (!ok) {
      setState(() {
        _printing = false;
        _connected = false;
        _statusMessage = '${device.name}-এ সংযোগ করা যায়নি।';
      });
      return;
    }

    setState(() {
      _connected = true;
      _statusMessage = 'রসিদ রাস্টারাইজ করে প্রিন্ট পাঠানো হচ্ছে...';
    });

    final pdfBytes = await widget.buildPdf();
    final printed = await _service.printPdfReceipt(
      pdfBytes: pdfBytes,
      paperSize: _paperSize,
    );

    if (!mounted) return;
    setState(() {
      _printing = false;
      _statusMessage = printed
          ? 'থার্মাল প্রিন্ট সফলভাবে সম্পন্ন হয়েছে।'
          : 'প্রিন্ট পাঠাতে সমস্যা হয়েছে। প্রিন্টারে কাগজ ও চার্জ পরীক্ষা করুন।';
    });

    if (printed) {
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('ব্লুটুথ থার্মাল প্রিন্ট সম্পন্ন হয়েছে'),
          backgroundColor: AppColors.emeraldPrimary,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(widget.title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
      content: SizedBox(
        width: double.maxFinite,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Text('পেপার সাইজ:', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600)),
                  const SizedBox(width: 8),
                  ChoiceChip(
                    label: const Text('৫৮ মিমি', style: TextStyle(fontSize: 11)),
                    selected: _paperSize == PaperSize.mm58,
                    onSelected: (_) => setState(() => _paperSize = PaperSize.mm58),
                  ),
                  const SizedBox(width: 6),
                  ChoiceChip(
                    label: const Text('৮০ মিমি', style: TextStyle(fontSize: 11)),
                    selected: _paperSize == PaperSize.mm80,
                    onSelected: (_) => setState(() => _paperSize = PaperSize.mm80),
                  ),
                ],
              ),
              const SizedBox(height: AppDimens.spacing12),
              if (_loading)
                const Center(
                  child: Padding(
                    padding: EdgeInsets.all(16),
                    child: CircularProgressIndicator(color: AppColors.emeraldPrimary),
                  ),
                )
              else if (!_btReady || _devices.isEmpty)
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.orange.withOpacity(0.08),
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: Colors.orange.withOpacity(0.3)),
                  ),
                  child: Text(
                    _statusMessage ?? 'কোনো ডিভাইস পাওয়া যায়নি',
                    style: const TextStyle(fontSize: 12),
                  ),
                )
              else ...[
                const Text(
                  'পেয়ার করা প্রিন্টার নির্বাচন করুন:',
                  style: TextStyle(fontSize: 12, color: Colors.grey),
                ),
                const SizedBox(height: 6),
                ..._devices.map((d) {
                  final selected = d.macAdress == _selectedMac && _connected;
                  return ListTile(
                    dense: true,
                    contentPadding: EdgeInsets.zero,
                    leading: Icon(
                      Icons.print_outlined,
                      color: selected ? AppColors.emeraldPrimary : Colors.blueGrey,
                    ),
                    title: Text(d.name.isNotEmpty ? d.name : 'Thermal Printer'),
                    subtitle: Text(d.macAdress, style: const TextStyle(fontSize: 11)),
                    trailing: _printing && d.macAdress == _selectedMac
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : ElevatedButton(
                            onPressed: _printing ? null : () => _connectAndPrint(d),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: AppColors.emeraldPrimary,
                              foregroundColor: Colors.white,
                            ),
                            child: const Text('প্রিন্ট', style: TextStyle(fontSize: 11)),
                          ),
                  );
                }),
              ],
              if (_statusMessage != null && _btReady && _devices.isNotEmpty) ...[
                const SizedBox(height: 8),
                Text(
                  _statusMessage!,
                  style: TextStyle(fontSize: 11, color: Colors.grey.shade700),
                ),
              ],
            ],
          ),
        ),
      ),
      actions: [
        TextButton.icon(
          onPressed: _printing ? null : _scanDevices,
          icon: const Icon(Icons.refresh, size: 16),
          label: const Text('রিস্ক্যান'),
        ),
        TextButton(
          onPressed: _printing ? null : () => Navigator.of(context).pop(),
          child: const Text('বন্ধ করুন'),
        ),
      ],
    );
  }
}
