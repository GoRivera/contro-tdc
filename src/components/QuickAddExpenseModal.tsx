import React, { useState } from 'react';
import { useApp } from '../context/AppContext.tsx';
import { CreditCard, Expense } from '../types.ts';
import { SPANISH_MONTHS } from '../domain/CreditCardCalculator.ts';
import { X, Sparkles, CreditCard as CardIcon, Check } from 'lucide-react';

interface QuickAddExpenseModalProps {
  isOpen: boolean;
  onClose: () => void;
  expenseToEdit?: Expense | null;
  defaultCardId?: number;
}

const QUICK_SUGGESTIONS = [
  { concept: 'Despensa Supermercado', category: 'Despensa' },
  { concept: 'Gasolina Carga', category: 'Gasolina' },
  { concept: 'Cena Restaurante', category: 'Restaurantes' },
  { concept: 'Farmacia / Salud', category: 'Salud' },
  { concept: 'Amazon México', category: 'Tecnología' },
  { concept: 'Uber / Transporte', category: 'Otros' },
];

const BENEFICIARIES = ['Personal', 'Memé', 'Ale', 'Poncho', 'Familiar', 'Hogar'];
const CATEGORIES = ['Despensa', 'Servicios', 'Tecnología', 'Gasolina', 'Restaurantes', 'Salud', 'Hogar', 'Otros'];
const MSI_MONTH_OPTIONS = [3, 6, 9, 12, 18, 24, 36];

export const QuickAddExpenseModal: React.FC<QuickAddExpenseModalProps> = ({
  isOpen,
  onClose,
  expenseToEdit,
  defaultCardId,
}) => {
  const { cards, addExpense, updateExpense } = useApp();

  const activeCards = cards.filter(c => c.isActive);
  const initialCardId = expenseToEdit?.cardId || defaultCardId || (activeCards[0]?.id ?? 1);

  const [cardId, setCardId] = useState<number>(initialCardId);
  const [concept, setConcept] = useState<string>(expenseToEdit?.concept || '');
  const [amount, setAmount] = useState<string>(expenseToEdit ? expenseToEdit.amount.toString() : '');
  const [beneficiary, setBeneficiary] = useState<string>(expenseToEdit?.beneficiary || 'Personal');
  const [category, setCategory] = useState<string>(expenseToEdit?.category || 'Despensa');
  const [isMsi, setIsMsi] = useState<boolean>(expenseToEdit?.isMsi || false);
  const [msiTotalMonths, setMsiTotalMonths] = useState<number>(expenseToEdit?.msiTotalMonths || 12);
  const [msiCurrentInstallment, setMsiCurrentInstallment] = useState<number>(expenseToEdit?.msiCurrentInstallment || 1);
  const [msiTotalPurchaseAmount, setMsiTotalPurchaseAmount] = useState<string>(
    expenseToEdit && expenseToEdit.msiTotalPurchaseAmount > 0
      ? expenseToEdit.msiTotalPurchaseAmount.toString()
      : ''
  );
  const [notes, setNotes] = useState<string>(expenseToEdit?.notes || '');
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
    if (!concept.trim()) {
      setError('Ingresa el concepto del gasto');
      return;
    }

    let calculatedTotalPurchase = parseFloat(msiTotalPurchaseAmount);
    let finalMonthlyAmount = parsedAmount;

    if (isMsi) {
      if (!isNaN(calculatedTotalPurchase) && calculatedTotalPurchase > 0) {
        finalMonthlyAmount = Math.round((calculatedTotalPurchase / msiTotalMonths) * 100) / 100;
      } else {
        calculatedTotalPurchase = parsedAmount * msiTotalMonths;
      }
    } else {
      calculatedTotalPurchase = 0;
    }

    if (expenseToEdit) {
      updateExpense({
        ...expenseToEdit,
        cardId,
        concept: concept.trim(),
        amount: finalMonthlyAmount,
        beneficiary,
        category,
        isMsi,
        msiTotalMonths: isMsi ? msiTotalMonths : 1,
        msiCurrentInstallment: isMsi ? msiCurrentInstallment : 1,
        msiTotalPurchaseAmount: calculatedTotalPurchase,
        notes: notes.trim(),
      });
    } else {
      addExpense({
        cardId,
        concept: concept.trim(),
        amount: finalMonthlyAmount,
        dateMillis: Date.now(),
        beneficiary,
        category,
        isMsi,
        msiTotalMonths: isMsi ? msiTotalMonths : 1,
        msiCurrentInstallment: isMsi ? msiCurrentInstallment : 1,
        msiTotalPurchaseAmount: calculatedTotalPurchase,
        notes: notes.trim(),
        targetStatementMonth: defaultTargetMonth,
      });
    }

    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in overflow-y-auto">
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl max-w-lg w-full shadow-2xl my-6 overflow-hidden">
        {/* Modal Header */}
        <div className="p-5 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between bg-slate-50 dark:bg-slate-800/50">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-blue-600/10 dark:bg-blue-500/20 text-blue-600 dark:text-blue-400 flex items-center justify-center font-bold">
              <CardIcon className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                {expenseToEdit ? 'Editar Gasto' : 'Registrar Nuevo Gasto'}
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Afecta el saldo y la línea de crédito de tu tarjeta
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
        <form onSubmit={handleSubmit} className="p-5 space-y-4 max-h-[80vh] overflow-y-auto">
          {error && (
            <div className="p-3 rounded-xl bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-xs font-semibold text-red-600 dark:text-red-400">
              {error}
            </div>
          )}

          {/* Quick Suggestions Chips */}
          <div>
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 block mb-1.5">
              Sugerencias rápidas:
            </span>
            <div className="flex flex-wrap gap-1.5">
              {QUICK_SUGGESTIONS.map(s => (
                <button
                  key={s.concept}
                  type="button"
                  onClick={() => {
                    setConcept(s.concept);
                    setCategory(s.category);
                  }}
                  className="px-2.5 py-1 text-xs rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-blue-50 dark:hover:bg-blue-950 hover:text-blue-600 transition-colors font-medium"
                >
                  {s.concept}
                </button>
              ))}
            </div>
          </div>

          {/* Select Card */}
          <div>
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
              Tarjeta de Crédito
            </label>
            <select
              value={cardId}
              onChange={e => setCardId(Number(e.target.value))}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm font-medium focus:ring-2 focus:ring-blue-500 outline-none"
            >
              {cards.map(c => (
                <option key={c.id} value={c.id}>
                  {c.name} ({c.bank}) •••• {c.last4Digits}
                </option>
              ))}
            </select>
          </div>

          {/* Concept Input */}
          <div>
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
              Concepto / Establecimiento
            </label>
            <input
              type="text"
              required
              placeholder="Ej: Despensa Chedraui, Amazon, Cena"
              value={concept}
              onChange={e => {
                setConcept(e.target.value);
                setError('');
              }}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm font-medium focus:ring-2 focus:ring-blue-500 outline-none"
            />
          </div>

          {/* Amount Input */}
          <div>
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
              {isMsi ? 'Monto Mensualidad ($)' : 'Monto Total de la Compra ($)'}
            </label>
            <div className="relative">
              <span className="absolute left-3.5 top-2.5 text-slate-400 font-bold">$</span>
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
                className="w-full pl-8 pr-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-mono text-base font-bold focus:ring-2 focus:ring-blue-500 outline-none"
              />
            </div>
          </div>

          {/* Beneficiary & Category */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
                Beneficiario
              </label>
              <select
                value={beneficiary}
                onChange={e => setBeneficiary(e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-xs font-medium focus:ring-2 focus:ring-blue-500 outline-none"
              >
                {BENEFICIARIES.map(b => (
                  <option key={b} value={b}>{b}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
                Categoría
              </label>
              <select
                value={category}
                onChange={e => setCategory(e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-xs font-medium focus:ring-2 focus:ring-blue-500 outline-none"
              >
                {CATEGORIES.map(c => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>
          </div>

          {/* MSI Section */}
          <div className="p-4 rounded-2xl bg-blue-50/60 dark:bg-blue-950/30 border border-blue-200/80 dark:border-blue-800/80">
            <div className="flex items-center justify-between mb-3">
              <div>
                <span className="text-sm font-bold text-slate-900 dark:text-white block">
                  Compra a Meses Sin Intereses (MSI)
                </span>
                <span className="text-xs text-slate-500 dark:text-slate-400">
                  Difiere este cargo en mensualidades fijas
                </span>
              </div>
              <input
                type="checkbox"
                id="is-msi-toggle"
                checked={isMsi}
                onChange={e => setIsMsi(e.target.checked)}
                className="w-5 h-5 accent-blue-600 rounded cursor-pointer"
              />
            </div>

            {isMsi && (
              <div className="space-y-3 pt-2 border-t border-blue-200/60 dark:border-blue-800/60">
                <div>
                  <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1.5">
                    Plazo total en meses:
                  </label>
                  <div className="flex flex-wrap gap-1.5">
                    {MSI_MONTH_OPTIONS.map(m => (
                      <button
                        key={m}
                        type="button"
                        onClick={() => setMsiTotalMonths(m)}
                        className={`px-3 py-1 rounded-lg text-xs font-bold transition-colors ${
                          msiTotalMonths === m
                            ? 'bg-blue-600 text-white shadow-sm'
                            : 'bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-blue-100'
                        }`}
                      >
                        {m} MSI
                      </button>
                    ))}
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">
                      Mensualidad Actual
                    </label>
                    <input
                      type="number"
                      min="1"
                      max={msiTotalMonths}
                      value={msiCurrentInstallment}
                      onChange={e => setMsiCurrentInstallment(parseInt(e.target.value, 10) || 1)}
                      className="w-full px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-xs font-bold"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">
                      Costo Total Compra (Opcional)
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      placeholder="Calculado automático"
                      value={msiTotalPurchaseAmount}
                      onChange={e => setMsiTotalPurchaseAmount(e.target.value)}
                      className="w-full px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-xs font-bold"
                    />
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Notes */}
          <div>
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider block mb-1.5">
              Notas Adicionales (Opcional)
            </label>
            <textarea
              rows={2}
              placeholder="Detalles sobre la compra, factura, etc."
              value={notes}
              onChange={e => setNotes(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-xs focus:ring-2 focus:ring-blue-500 outline-none resize-none"
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
              className="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm shadow-md shadow-blue-500/25 transition-colors"
            >
              {expenseToEdit ? 'Guardar Cambios' : 'Registrar Gasto'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
