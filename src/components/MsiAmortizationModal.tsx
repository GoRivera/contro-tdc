import React from 'react';
import { MsiSummary, InstallmentStatus, AmortizationRow } from '../types.ts';
import { SPANISH_MONTHS, formatCurrency, getMsiBaseCalendar } from '../domain/CreditCardCalculator.ts';
import { useApp } from '../context/AppContext.tsx';
import { X, CheckCircle, Clock, AlertCircle, Calendar, CreditCard as CardIcon, ChevronRight, Plus, Minus, Calculator } from 'lucide-react';

interface MsiAmortizationModalProps {
  msiSummary: MsiSummary | null;
  onClose: () => void;
  onOpenMinimumPayoff?: (balance: number, rate: number, limit: number) => void;
}

export const MsiAmortizationModal: React.FC<MsiAmortizationModalProps> = ({
  msiSummary,
  onClose,
  onOpenMinimumPayoff,
}) => {
  const { isPrivateMode, advanceMsiInstallment, undoAdvanceMsiInstallment } = useApp();

  if (!msiSummary) return null;

  const { expense, card, monthlyPayment, totalPurchaseAmount, remainingBalance, isCompleted } = msiSummary;
  const totalMonths = Math.max(1, expense.msiTotalMonths);
  const currentInst = Math.max(0, Math.min(expense.msiCurrentInstallment, totalMonths));

  // Build the amortization rows month by month
  const baseCal = getMsiBaseCalendar(expense);
  const rows: AmortizationRow[] = [];

  for (let i = 1; i <= totalMonths; i++) {
    const rowDate = new Date(baseCal);
    rowDate.setMonth(rowDate.getMonth() + (i - currentInst));
    const monthLabel = `${SPANISH_MONTHS[rowDate.getMonth()]} ${rowDate.getFullYear()}`;

    const remainingAfter = Math.max(0, totalPurchaseAmount - i * monthlyPayment);

    let status: InstallmentStatus = InstallmentStatus.PENDING;
    if (i < currentInst) {
      status = InstallmentStatus.PAID;
    } else if (i === currentInst) {
      status = isCompleted ? InstallmentStatus.PAID : InstallmentStatus.CURRENT;
    } else {
      status = InstallmentStatus.PENDING;
    }

    rows.push({
      installmentNumber: i,
      monthYearLabel: monthLabel,
      paymentAmount: monthlyPayment,
      remainingBalanceAfter: remainingAfter,
      status,
    });
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl max-w-xl w-full max-h-[90vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Modal Header */}
        <div className="p-5 border-b border-slate-200 dark:border-slate-800 flex items-start justify-between bg-slate-50 dark:bg-slate-800/50">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-bold uppercase tracking-wider px-2.5 py-0.5 rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300">
                Plan {totalMonths} Meses Sin Intereses
              </span>
              <span className="text-xs text-slate-500 dark:text-slate-400">
                {card?.name || 'TDC'}
              </span>
            </div>
            <h2 className="text-xl font-extrabold text-slate-900 dark:text-white">
              {expense.concept}
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              {expense.category} • Beneficiario: {expense.beneficiary}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 dark:hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Overview KPI Cards */}
        <div className="p-5 border-b border-slate-100 dark:border-slate-800 grid grid-cols-3 gap-3 bg-white dark:bg-slate-900">
          <div className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-700/60">
            <span className="text-[10px] uppercase font-bold text-slate-500 dark:text-slate-400 block mb-1">
              Monto Total
            </span>
            <p className="text-base font-extrabold text-slate-900 dark:text-white">
              {formatCurrency(totalPurchaseAmount, isPrivateMode)}
            </p>
          </div>
          <div className="p-3 rounded-2xl bg-blue-50/70 dark:bg-blue-950/40 border border-blue-200/60 dark:border-blue-800/60">
            <span className="text-[10px] uppercase font-bold text-blue-600 dark:text-blue-400 block mb-1">
              Mensualidad
            </span>
            <p className="text-base font-extrabold text-blue-700 dark:text-blue-300">
              {formatCurrency(monthlyPayment, isPrivateMode)}
            </p>
          </div>
          <div className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-700/60">
            <span className="text-[10px] uppercase font-bold text-slate-500 dark:text-slate-400 block mb-1">
              Saldo Pendiente
            </span>
            <p className="text-base font-extrabold text-slate-900 dark:text-white">
              {formatCurrency(remainingBalance, isPrivateMode)}
            </p>
          </div>
        </div>

        {/* Progress & Fast Step Controls */}
        <div className="px-5 py-3 bg-slate-50 dark:bg-slate-800/30 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-blue-100 dark:bg-blue-900/50 flex items-center justify-center font-bold text-blue-700 dark:text-blue-300 text-sm">
              {currentInst}/{totalMonths}
            </div>
            <div>
              <span className="text-xs font-semibold text-slate-900 dark:text-white block">
                {isCompleted ? '¡Plan Completado!' : `Mensualidad ${currentInst} de ${totalMonths}`}
              </span>
              <div className="w-32 sm:w-44 bg-slate-200 dark:bg-slate-700 rounded-full h-2 mt-1 overflow-hidden">
                <div
                  className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${Math.round((currentInst / totalMonths) * 100)}%` }}
                />
              </div>
            </div>
          </div>

          <div className="flex items-center gap-1.5">
            <button
              onClick={() => undoAdvanceMsiInstallment(expense.id)}
              disabled={currentInst <= 1}
              title="Retroceder una mensualidad"
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40 disabled:pointer-events-none transition-colors"
            >
              <Minus className="w-4 h-4" />
            </button>
            <button
              onClick={() => advanceMsiInstallment(expense.id)}
              disabled={currentInst >= totalMonths}
              title="Avanzar / Liquidar siguiente mensualidad"
              className="p-2 rounded-xl bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-40 disabled:pointer-events-none transition-colors"
            >
              <Plus className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Amortization Table */}
        <div className="flex-1 overflow-y-auto p-5">
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
            Calendario de Mensualidades y Amortización
          </h4>
          <div className="divide-y divide-slate-100 dark:divide-slate-800 border border-slate-200/80 dark:border-slate-800 rounded-2xl overflow-hidden">
            {rows.map(row => (
              <div
                key={row.installmentNumber}
                className={`p-3.5 flex items-center justify-between text-sm transition-colors ${
                  row.status === InstallmentStatus.CURRENT
                    ? 'bg-blue-50/50 dark:bg-blue-950/20 font-medium'
                    : 'hover:bg-slate-50/50 dark:hover:bg-slate-800/50'
                }`}
              >
                <div className="flex items-center gap-3">
                  <span className="w-6 text-xs font-bold text-slate-400 text-center">
                    #{row.installmentNumber}
                  </span>
                  <div>
                    <span className="font-semibold text-slate-900 dark:text-white block">
                      {row.monthYearLabel}
                    </span>
                    <span className="text-xs text-slate-400">
                      Saldo restante: {formatCurrency(row.remainingBalanceAfter, isPrivateMode)}
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <span className="font-mono font-bold text-slate-900 dark:text-slate-100">
                    {formatCurrency(row.paymentAmount, isPrivateMode)}
                  </span>
                  <div>
                    {row.status === InstallmentStatus.PAID && (
                      <span className="inline-flex items-center gap-1 text-[11px] font-semibold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
                        <CheckCircle className="w-3 h-3" /> Pagada
                      </span>
                    )}
                    {row.status === InstallmentStatus.CURRENT && (
                      <span className="inline-flex items-center gap-1 text-[11px] font-semibold px-2 py-0.5 rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300 animate-pulse">
                        <Clock className="w-3 h-3" /> Cobro actual
                      </span>
                    )}
                    {row.status === InstallmentStatus.PENDING && (
                      <span className="inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400">
                        Pendiente
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Modal Footer */}
        <div className="p-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-850 flex items-center justify-between">
          {onOpenMinimumPayoff && card && (
            <button
              onClick={() => {
                onClose();
                onOpenMinimumPayoff(
                  remainingBalance,
                  card.annualInterestRatePercent,
                  card.creditLimit
                );
              }}
              className="text-xs font-semibold text-indigo-600 dark:text-indigo-400 hover:underline flex items-center gap-1.5"
            >
              <Calculator className="w-4 h-4" />
              Simular pago mínimo
            </button>
          )}
          <button
            onClick={onClose}
            className="ml-auto px-5 py-2.5 rounded-xl bg-slate-900 text-white dark:bg-white dark:text-slate-900 font-bold text-sm hover:opacity-90 transition-opacity"
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
};
