import React, { useState, useMemo } from 'react';
import { simulateMinimumPaymentPayoff, formatCurrency } from '../domain/CreditCardCalculator.ts';
import { useApp } from '../context/AppContext.tsx';
import { X, Calculator, AlertTriangle, ShieldCheck, TrendingDown } from 'lucide-react';

interface MinimumPaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialBalance?: number;
  initialRate?: number;
  initialLimit?: number;
}

export const MinimumPaymentModal: React.FC<MinimumPaymentModalProps> = ({
  isOpen,
  onClose,
  initialBalance = 15000,
  initialRate = 55.0,
  initialLimit = 40000,
}) => {
  const { isPrivateMode } = useApp();

  const [balance, setBalance] = useState<number>(initialBalance || 15000);
  const [annualRate, setAnnualRate] = useState<number>(initialRate || 55.0);
  const [creditLimit, setCreditLimit] = useState<number>(initialLimit || 40000);

  const simulation = useMemo(() => {
    return simulateMinimumPaymentPayoff(balance, annualRate, creditLimit);
  }, [balance, annualRate, creditLimit]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in overflow-y-auto">
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl max-w-xl w-full shadow-2xl my-6 overflow-hidden">
        {/* Header */}
        <div className="p-5 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between bg-amber-50/70 dark:bg-amber-950/30">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-amber-500/15 text-amber-700 dark:text-amber-400 flex items-center justify-center font-bold">
              <Calculator className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                Simulador Oficial de Pago Mínimo
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Fórmula oficial Banxico con tasa ordinaria + 16% IVA
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 dark:hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 space-y-5 max-h-[80vh] overflow-y-auto">
          {/* Controls */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="text-[11px] font-bold text-slate-600 dark:text-slate-300 uppercase tracking-wider block mb-1">
                Saldo a Financiar
              </label>
              <input
                type="number"
                step="100"
                value={balance}
                onChange={e => setBalance(Math.max(0, parseFloat(e.target.value) || 0))}
                className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm font-bold"
              />
            </div>
            <div>
              <label className="text-[11px] font-bold text-slate-600 dark:text-slate-300 uppercase tracking-wider block mb-1">
                Tasa Anual (%)
              </label>
              <input
                type="number"
                step="1"
                value={annualRate}
                onChange={e => setAnnualRate(Math.max(1, parseFloat(e.target.value) || 1))}
                className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm font-bold"
              />
            </div>
            <div>
              <label className="text-[11px] font-bold text-slate-600 dark:text-slate-300 uppercase tracking-wider block mb-1">
                Límite Tarjeta ($)
              </label>
              <input
                type="number"
                step="1000"
                value={creditLimit}
                onChange={e => setCreditLimit(Math.max(0, parseFloat(e.target.value) || 0))}
                className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm font-bold"
              />
            </div>
          </div>

          {/* Alert / Warning Banner */}
          <div className="p-4 rounded-2xl bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800/60 flex items-start gap-3">
            <AlertTriangle className="w-5 h-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
            <div className="text-xs text-amber-900 dark:text-amber-200">
              <p className="font-bold mb-0.5">¡Pagar solo el mínimo es el crédito más costoso!</p>
              <p>
                Tardarías <span className="font-extrabold underline">{simulation.monthsToPayOff} meses</span> ({Math.round((simulation.monthsToPayOff / 12) * 10) / 10} años) en liquidar esta deuda solo con pagos mínimos, pagando más del doble del importe original.
              </p>
            </div>
          </div>

          {/* Results Comparison Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
            <div className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700">
              <span className="text-[10px] uppercase font-bold text-slate-500 block mb-1">
                1er Pago Mínimo
              </span>
              <p className="text-base font-extrabold text-slate-900 dark:text-white">
                {formatCurrency(simulation.firstMonthMinimumPayment, isPrivateMode)}
              </p>
            </div>
            <div className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700">
              <span className="text-[10px] uppercase font-bold text-slate-500 block mb-1">
                Tiempo Total
              </span>
              <p className="text-base font-extrabold text-amber-600 dark:text-amber-400">
                {simulation.monthsToPayOff} meses
              </p>
            </div>
            <div className="p-3 rounded-2xl bg-red-50/70 dark:bg-red-950/30 border border-red-200/60 dark:border-red-900/40">
              <span className="text-[10px] uppercase font-bold text-red-600 dark:text-red-400 block mb-1">
                Intereses + IVA
              </span>
              <p className="text-base font-extrabold text-red-700 dark:text-red-300">
                {formatCurrency(simulation.totalInterestPaid + simulation.totalIvaPaid, isPrivateMode)}
              </p>
            </div>
            <div className="p-3 rounded-2xl bg-emerald-50/70 dark:bg-emerald-950/30 border border-emerald-200/60 dark:border-emerald-900/40">
              <span className="text-[10px] uppercase font-bold text-emerald-600 dark:text-emerald-400 block mb-1">
                Ahorro Pago Total
              </span>
              <p className="text-base font-extrabold text-emerald-700 dark:text-emerald-300">
                {formatCurrency(simulation.savingsPayingInFull, isPrivateMode)}
              </p>
            </div>
          </div>

          {/* Sample Table */}
          {simulation.monthlyAmortizationSample.length > 0 && (
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
                Evolución proyectada de la deuda
              </h4>
              <div className="border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden text-xs">
                <table className="w-full text-left">
                  <thead className="bg-slate-50 dark:bg-slate-800 text-slate-500 font-bold border-b border-slate-200 dark:border-slate-700">
                    <tr>
                      <th className="p-2.5">Mes</th>
                      <th className="p-2.5">Saldo Inicio</th>
                      <th className="p-2.5">Pago Mínimo</th>
                      <th className="p-2.5">Interés+IVA</th>
                      <th className="p-2.5">Saldo Final</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 dark:divide-slate-800 font-mono">
                    {simulation.monthlyAmortizationSample.map(item => (
                      <tr key={item.month} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/40">
                        <td className="p-2.5 font-bold text-slate-700 dark:text-slate-300">#{item.month}</td>
                        <td className="p-2.5">{formatCurrency(item.startBalance, isPrivateMode)}</td>
                        <td className="p-2.5 font-semibold text-blue-600 dark:text-blue-400">{formatCurrency(item.minimumPayment, isPrivateMode)}</td>
                        <td className="p-2.5 text-red-600 dark:text-red-400">{formatCurrency(item.interest + item.iva, isPrivateMode)}</td>
                        <td className="p-2.5 font-bold text-slate-900 dark:text-white">{formatCurrency(item.endBalance, isPrivateMode)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-850 flex items-center justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2.5 rounded-xl bg-slate-900 text-white dark:bg-white dark:text-slate-900 font-bold text-sm hover:opacity-90 transition-opacity"
          >
            Entendido
          </button>
        </div>
      </div>
    </div>
  );
};
