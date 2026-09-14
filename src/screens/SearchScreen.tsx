import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext.tsx';
import { formatCurrency, formatDate } from '../domain/CreditCardCalculator.ts';
import { Expense, Payment } from '../types.ts';
import { Search as SearchIcon, Filter, X, ArrowDownLeft, Trash2, Edit2 } from 'lucide-react';

interface SearchScreenProps {
  onEditExpense: (expense: Expense) => void;
}

export const SearchScreen: React.FC<SearchScreenProps> = ({ onEditExpense }) => {
  const { cards, expenses, payments, deleteExpense, deletePayment, isPrivateMode } = useApp();

  const [query, setQuery] = useState<string>('');
  const [selectedCardId, setSelectedCardId] = useState<number | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');

  // Filtered expenses
  const filteredExpenses = useMemo(() => {
    const q = query.toLowerCase().trim();
    return expenses.filter(exp => {
      if (selectedCardId !== null && exp.cardId !== selectedCardId) return false;
      if (selectedCategory !== 'ALL' && exp.category !== selectedCategory) return false;
      if (!q) return true;

      return (
        exp.concept.toLowerCase().includes(q) ||
        exp.beneficiary.toLowerCase().includes(q) ||
        exp.category.toLowerCase().includes(q) ||
        exp.targetStatementMonth.toLowerCase().includes(q) ||
        (exp.notes && exp.notes.toLowerCase().includes(q))
      );
    });
  }, [expenses, query, selectedCardId, selectedCategory]);

  // Filtered payments
  const filteredPayments = useMemo(() => {
    const q = query.toLowerCase().trim();
    return payments.filter(pay => {
      if (selectedCardId !== null && pay.cardId !== selectedCardId) return false;
      if (!q) return true;

      return (
        pay.concept.toLowerCase().includes(q) ||
        pay.sourcePayer.toLowerCase().includes(q) ||
        pay.targetStatementMonth.toLowerCase().includes(q) ||
        (pay.notes && pay.notes.toLowerCase().includes(q))
      );
    });
  }, [payments, query, selectedCardId]);

  const categories = useMemo(() => {
    const set = new Set(expenses.map(e => e.category));
    return Array.from(set);
  }, [expenses]);

  return (
    <div className="space-y-6 pb-12">
      {/* Search Input Bar */}
      <div className="p-4 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
        <div className="relative">
          <SearchIcon className="absolute left-4 top-3.5 w-5 h-5 text-slate-400" />
          <input
            type="text"
            placeholder="Buscar por concepto, comercio, beneficiario (Ale, Memé), notas..."
            value={query}
            onChange={e => setQuery(e.target.value)}
            className="w-full pl-12 pr-10 py-3 rounded-2xl bg-slate-50 dark:bg-slate-800/70 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white text-sm font-medium focus:ring-2 focus:ring-blue-500 outline-none"
          />
          {query && (
            <button
              onClick={() => setQuery('')}
              className="absolute right-3.5 top-3.5 p-1 text-slate-400 hover:text-slate-600"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-2 pt-1 text-xs">
          <span className="text-slate-400 font-semibold flex items-center gap-1">
            <Filter className="w-3.5 h-3.5" /> Filtrar:
          </span>

          {/* Card filter */}
          <select
            value={selectedCardId ?? ''}
            onChange={e => setSelectedCardId(e.target.value ? Number(e.target.value) : null)}
            className="px-2.5 py-1 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 font-medium outline-none"
          >
            <option value="">Todas las tarjetas</option>
            {cards.map(c => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>

          {/* Category filter */}
          <select
            value={selectedCategory}
            onChange={e => setSelectedCategory(e.target.value)}
            className="px-2.5 py-1 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 font-medium outline-none"
          >
            <option value="ALL">Todas las categorías</option>
            {categories.map(cat => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Results stats */}
      <div className="flex items-center justify-between text-xs text-slate-500 font-medium">
        <span>
          Encontrados: <strong>{filteredExpenses.length}</strong> gastos y{' '}
          <strong>{filteredPayments.length}</strong> abonos
        </span>
      </div>

      {/* Results List */}
      <div className="space-y-4">
        {/* Expenses List */}
        {filteredExpenses.length > 0 && (
          <div className="p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">
              Gastos ({filteredExpenses.length})
            </h4>

            <div className="divide-y divide-slate-100 dark:divide-slate-800">
              {filteredExpenses.map(exp => {
                const card = cards.find(c => c.id === exp.cardId);

                return (
                  <div
                    key={exp.id}
                    className="py-3 flex items-center justify-between text-xs hover:bg-slate-50/50 dark:hover:bg-slate-800/50 rounded-xl px-2 transition-colors"
                  >
                    <div>
                      <div className="flex items-center gap-2 mb-0.5">
                        <span className="font-bold text-slate-900 dark:text-white text-sm">
                          {exp.concept}
                        </span>
                        {exp.isMsi && (
                          <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300">
                            {exp.msiCurrentInstallment}/{exp.msiTotalMonths} MSI
                          </span>
                        )}
                        <span className="text-[11px] text-slate-400">
                          {card?.name}
                        </span>
                      </div>
                      <span className="text-slate-400 text-[11px]">
                        {formatDate(exp.dateMillis)} • {exp.beneficiary} • {exp.category} • {exp.targetStatementMonth}
                      </span>
                    </div>

                    <div className="flex items-center gap-3">
                      <span className="font-mono font-bold text-sm text-slate-900 dark:text-white">
                        {formatCurrency(exp.amount, isPrivateMode)}
                      </span>
                      <button
                        onClick={() => onEditExpense(exp)}
                        className="p-1 text-slate-400 hover:text-slate-700"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => {
                          if (confirm(`¿Eliminar el gasto "${exp.concept}"?`)) {
                            deleteExpense(exp.id);
                          }
                        }}
                        className="p-1 text-slate-400 hover:text-red-500"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Payments List */}
        {filteredPayments.length > 0 && (
          <div className="p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-400">
              Abonos / Pagos ({filteredPayments.length})
            </h4>

            <div className="divide-y divide-slate-100 dark:divide-slate-800">
              {filteredPayments.map(pay => {
                const card = cards.find(c => c.id === pay.cardId);

                return (
                  <div
                    key={pay.id}
                    className="py-3 flex items-center justify-between text-xs hover:bg-emerald-50/50 dark:hover:bg-emerald-950/20 rounded-xl px-2 transition-colors"
                  >
                    <div className="flex items-center gap-2.5">
                      <div className="w-7 h-7 rounded-lg bg-emerald-100 dark:bg-emerald-900/50 flex items-center justify-center font-bold text-emerald-600 dark:text-emerald-400">
                        <ArrowDownLeft className="w-4 h-4" />
                      </div>
                      <div>
                        <span className="font-bold text-slate-900 dark:text-white block text-sm">
                          {pay.concept}
                        </span>
                        <span className="text-slate-400 text-[11px]">
                          {formatDate(pay.dateMillis)} • Pagado por {pay.sourcePayer} • {card?.name}
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center gap-3">
                      <span className="font-mono font-bold text-sm text-emerald-700 dark:text-emerald-300">
                        -{formatCurrency(pay.amount, isPrivateMode)}
                      </span>
                      <button
                        onClick={() => {
                          if (confirm(`¿Eliminar el abono "${pay.concept}"?`)) {
                            deletePayment(pay.id);
                          }
                        }}
                        className="p-1 text-slate-400 hover:text-red-500"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {filteredExpenses.length === 0 && filteredPayments.length === 0 && (
          <div className="p-12 text-center rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-400 text-xs">
            No se encontraron movimientos que coincidan con la búsqueda.
          </div>
        )}
      </div>
    </div>
  );
};
