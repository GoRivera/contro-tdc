import React, { useState } from 'react';
import { useApp } from '../context/AppContext.tsx';
import { SPANISH_MONTHS } from '../domain/CreditCardCalculator.ts';
import { X, CheckCircle2, ArrowDownLeft } from 'lucide-react';

interface QuickAddPaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  defaultCardId?: number;
}

const PAYMENT_SUGGESTIONS = [
  'Pago total para no generar intereses',
  'Abono a capital',
  'Pago mínimo',
  'Devolución de comercio',
  'Reembolso de compra',
];

const PAYERS = ['Personal', 'Memé', 'Ale', 'Poncho', 'Banco / Reembolso'];

export const QuickAddPaymentModal: React.FC<QuickAddPaymentModalProps> = ({
  isOpen,
  onClose,
  defaultCardId,
}) => {
  const { cards, addPayment } = useApp();

  const activeCards = cards.filter(c => c.isActive);
  const initialCardId = defaultCardId || (activeCards[0]?.id ?? 1);

  const [cardId, setCardId] = useState<number>(initialCardId);
  const [concept, setConcept] = useState<string>('Pago total para no generar intereses');
  const [amount, setAmount] = useState<string>('');
  const [sourcePayer, setSourcePayer] = useState<string>('Personal');
  const [notes, setNotes] = useState<string>('');
  const [error, setError] = useState<string>('');

  if (!isOpen) return null;

  const currentYear = new Date().getFullYear();
  const currentMonthName = SPANISH_MONTHS[new Date().getMonth()];
  const defaultTargetMonth = `${currentMonthName} ${currentYear}`;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Por favor ingresa un monto válido mayor a 0');
      return;
    }

    addPayment({
      cardId,
      concept: concept.trim() || 'Abono a Tarjeta',
      amount: parsedAmount,
      dateMillis: Date.now(),
      sourcePayer,
      targetStatementMonth: defaultTargetMonth,
      notes: notes.trim(),
    });

    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in overflow-y-auto">
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl max-w-lg w-full shadow-2xl my-6 overflow-hidden">
        {/* Modal Header */}
        <div className="p-5 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between bg-emerald-50/70 dark:bg-emerald-950/30">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-emerald-600/10 dark:bg-emerald-500/20 text-emerald-600 dark:text-emerald-400 flex items-center justify-center font-bold">
              <ArrowDownLeft className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                Registrar Abono / Pago a Tarjeta
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Libera línea de crédito y amortiza tu saldo deudor
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

        {/* Modal Body */}
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {error && (
            <div className="p-3 rounded-xl bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-xs font-semibold text-red-600 dark:text-red-400">
              {error}
            </div>
          )}

          {/* Select Card */}
          <div>
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
              Tarjeta que Recibe el Pago
            </label>
            <select
              value={cardId}
              onChange={e => setCardId(Number(e.target.value))}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm font-medium focus:ring-2 focus:ring-emerald-500 outline-none"
            >
              {cards.map(c => (
                <option key={c.id} value={c.id}>
                  {c.name} ({c.bank}) •••• {c.last4Digits}
                </option>
              ))}
            </select>
          </div>

          {/* Amount */}
          <div>
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
              Monto del Abono ($ MXN)
            </label>
            <div className="relative">
              <span className="absolute left-3.5 top-2.5 text-emerald-600 font-bold">$</span>
              <input
                type="number"
                step="0.01"
                min="0.01"
                required
                placeholder="0.00"
                value={amount}
                onChange={e => {
                  setAmount(e.target.value);
                  setError('');
                }}
                className="w-full pl-8 pr-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-mono text-base font-bold focus:ring-2 focus:ring-emerald-500 outline-none"
              />
            </div>
          </div>

          {/* Concept Chips */}
          <div>
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
              Concepto del Abono
            </label>
            <input
              type="text"
              required
              value={concept}
              onChange={e => setConcept(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm font-medium focus:ring-2 focus:ring-emerald-500 outline-none mb-2"
            />
            <div className="flex flex-wrap gap-1.5">
              {PAYMENT_SUGGESTIONS.map(s => (
                <button
                  key={s}
                  type="button"
                  onClick={() => setConcept(s)}
                  className={`px-2.5 py-1 text-xs rounded-lg transition-colors font-medium ${
                    concept === s
                      ? 'bg-emerald-600 text-white shadow-sm'
                      : 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-emerald-50 dark:hover:bg-emerald-950'
                  }`}
                >
                  {s}
                </button>
              ))}
            </div>
          </div>

          {/* Source Payer */}
          <div>
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
              ¿Quién realiza el abono?
            </label>
            <select
              value={sourcePayer}
              onChange={e => setSourcePayer(e.target.value)}
              className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-xs font-medium focus:ring-2 focus:ring-emerald-500 outline-none"
            >
              {PAYERS.map(p => (
                <option key={p} value={p}>{p}</option>
              ))}
            </select>
          </div>

          {/* Notes */}
          <div>
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
              Notas (Opcional)
            </label>
            <textarea
              rows={2}
              placeholder="Ej: Transferencia SPEI desde cuenta nómina BBVA"
              value={notes}
              onChange={e => setNotes(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-xs focus:ring-2 focus:ring-emerald-500 outline-none resize-none"
            />
          </div>

          {/* Submit */}
          <div className="pt-2 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 font-semibold text-sm hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm shadow-md shadow-emerald-600/25 transition-colors flex items-center gap-1.5"
            >
              <CheckCircle2 className="w-4 h-4" />
              Aplicar Abono
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
