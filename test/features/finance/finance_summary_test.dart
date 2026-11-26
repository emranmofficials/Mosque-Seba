import 'package:flutter_test/flutter_test.dart';
import 'package:mosqueseba/features/finance/models/finance_transaction_model.dart';
import 'package:mosqueseba/features/finance/repositories/finance_repository.dart';

void main() {
  group('FinanceRepository', () {
    final repo = FinanceRepository();

    test('calculates summary excluding pending, rejected, and reversed items', () {
      const txns = [
        FinanceTransactionModel(
          id: '1',
          transactionCode: 'TXN-1',
          categoryNameBn: 'চাঁদা',
          type: 'income',
          amount: 10000,
          txnDate: '2026-11-01',
          createdBy: 'Treasurer',
          approvalStatus: 'approved',
          createdAt: 1,
        ),
        FinanceTransactionModel(
          id: '2',
          transactionCode: 'TXN-2',
          categoryNameBn: 'বিদ্যুৎ',
          type: 'expense',
          amount: 2000,
          txnDate: '2026-11-02',
          createdBy: 'Treasurer',
          approvalStatus: 'approved',
          createdAt: 2,
        ),
        FinanceTransactionModel(
          id: '3',
          transactionCode: 'TXN-3',
          categoryNameBn: 'মেরামত',
          type: 'expense',
          amount: 15000,
          txnDate: '2026-11-03',
          createdBy: 'Treasurer',
          approvalStatus: 'pending',
          createdAt: 3,
        ),
        FinanceTransactionModel(
          id: '4',
          transactionCode: 'TXN-4',
          categoryNameBn: 'বাতিল',
          type: 'expense',
          amount: 5000,
          txnDate: '2026-11-04',
          createdBy: 'Treasurer',
          approvalStatus: 'rejected',
          createdAt: 4,
        ),
        FinanceTransactionModel(
          id: '5',
          transactionCode: 'TXN-5',
          categoryNameBn: 'রিভার্সড',
          type: 'income',
          amount: 3000,
          txnDate: '2026-11-05',
          createdBy: 'Treasurer',
          approvalStatus: 'approved',
          isReversal: true,
          createdAt: 5,
        ),
      ];

      final summary = repo.calculateSummary(txns);
      expect(summary.totalIncome, 10000);
      expect(summary.totalExpense, 2000);
      expect(summary.netBalance, 8000);
      expect(summary.pendingVouchersCount, 1);
    });

    test('prevents self-approval of a voucher', () async {
      await expectLater(
        () => repo.reviewTransaction(
          transactionId: 'txn-1',
          reviewerName: 'President',
          creatorName: ' president ',
          isApproved: true,
        ),
        throwsException,
      );
    });
  });
}
