import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext.tsx';
import { Subscription } from '../types.ts';
import { formatCurrency, SPANISH_MONTHS } from '../domain/CreditCardCalculator.ts';
import {
  Repeat,
  Plus,
  CreditCard as CardIcon,
  Calendar,
  Users,
  CheckCircle2,
  Trash2,
  Edit2,
  X,
  Clock,
  Sparkles,
  ArrowRight,
} from 'lucide-react';

interface SubscriptionsScreenProps {
  onAddExpenseFromSub?: (sub: Subscription) => void;
}

export const SubscriptionsScreen: React.FC<SubscriptionsScreenProps> = () => {
  const { cards, subscriptions, addSubscription, updateSubscription, deleteSubscription, addExpense, isPrivateMode } = useApp();

  const [isAddModalOpen, setIsAddModalOpen] = useState<boolean>(false);
  const [subToEdit, setSubToEdit] = useState<Subscription | null>(null);

  // Form states
  const [name, setName] = useState<string>('');
  const [cardId, setCardId] = useState<number>(cards[0]?.id || 1);
  const [billingDayOfMonth, setBillingDayOfMonth] = useState<number>(1);
  const [totalMonthlyAmount, setTotalMonthlyAmount] = useState<string>('');
  const [category, setCategory] = useState<string>('Streaming');
  const [periodicity, setPeriodicity] = useState<Subscription['periodicity']>('MENSUAL');
  const [participantsSummary, setParticipantsSummary] = useState<string>('');
  const [notes, setNotes] = useState<string>('');

  const activeSubs = useMemo(() => subscriptions.filter(s => s.isActive), [subscriptions]);

  const totalMonthlyCommitment = useMemo(() => {
    return activeSubs.reduce((sum, s) => {
      let monthlyVal = s.totalMonthlyAmount;
      if (s.periodicity === 'BIMESTRAL') monthlyVal /= 2;
      if (s.periodicity === 'TRIMESTRAL') monthlyVal /= 3;
      if (s.periodicity === 'SEMESTRAL') monthlyVal /= 6;
      if (s.periodicity === 'ANUAL') monthlyVal /= 12;
      return sum + monthlyVal;
    }, 0);
  }, [activeSubs]);

  const openCreateModal = () => {
    setSubToEdit(null);
    setName('');
    setCardId(cards[0]?.id || 1);
    setBillingDayOfMonth(1);
    setTotalMonthlyAmount('');
    setCategory('Streaming');
    setPeriodicity('MENSUAL');
    setParticipantsSummary('');
    setNotes('');
    setIsAddModalOpen(true);
  };

  const openEditModal = (sub: Subscription) => {
    setSubToEdit(sub);
    setName(sub.name);
    setCardId(sub.cardId);
    setBillingDayOfMonth(sub.billingDayOfMonth);
    setTotalMonthlyAmount(sub.totalMonthlyAmount.toString());
    setCategory(sub.category);
    setPeriodicity(sub.periodicity);
    setParticipantsSummary(sub.participantsSummary);
    setNotes(sub.notes);
    setIsAddModalOpen(true);
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const amountNum = parseFloat(totalMonthlyAmount);
    if (isNaN(amountNum) || amountNum <= 0) return;

    if (subToEdit) {
      updateSubscription({
        ...subToEdit,
        name: name.trim(),
        cardId,
        billingDayOfMonth,
        totalMonthlyAmount: amountNum,
        category,
        periodicity,
        participantsSummary: participantsSummary.trim(),
        notes: notes.trim(),
      });
    } else {
      addSubscription({
        name: name.trim(),
        cardId,
        billingDayOfMonth,
        totalMonthlyAmount: amountNum,
        category,
        startMonth: `${SPANISH_MONTHS[new Date().getMonth()]} ${new Date().getFullYear()}`,
        isActive: true,
        periodicity,
        participantsSummary: participantsSummary.trim(),
        notes: notes.trim(),
      });
    }
    setIsAddModalOpen(false);
  };

  const recordSubAsExpense = (sub: Subscription) => {
    const currentMonth = `${SPANISH_MONTHS[new Date().getMonth()]} ${new Date().getFullYear()}`;
    addExpense({
      cardId: sub.cardId,
      concept: `Suscripción ${sub.name}`,
      amount: sub.totalMonthlyAmount,
      dateMillis: Date.now(),
      beneficiary: 'Personal',
      category: 'Servicios',
      isMsi: false,
      msiTotalMonths: 1,
      msiCurrentInstallment: 1,
      msiTotalPurchaseAmount: 0,
      notes: `Cargo periódico automático • ${sub.participantsSummary || ''}`,
      targetStatementMonth: currentMonth,
      isSubscription: true,
      subscriptionId: sub.id,
    });
    alert(`Se registró el cargo de ${sub.name} por ${formatCurrency(sub.totalMonthlyAmount)} en tu tarjeta.`);
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Hero KPI Card */}
      <div className="bg-gradient-to-br from-purple-900 via-indigo-950 to-slate-900 text-white rounded-3xl p-6 shadow-xl border border-purple-500/20 relative overflow-hidden">
        <div className="relative z-10 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1.5">
              <Repeat className="w-4 h-4 text-purple-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-purple-300">
                Gasto Mensual en Suscripciones
              </span>
            </div>
            <div className="text-3xl sm:text-4xl font-black font-mono tracking-tight text-white">
              {formatCurrency(totalMonthlyCommitment, isPrivateMode)}
              <span className="text-sm font-normal text-purple-300"> /mes</span>
            </div>
            <p className="text-xs text-purple-200 mt-1">
              {activeSubs.length} servicios domiciliados a tus tarjetas de crédito
            </p>
          </div>

          <button
            onClick={openCreateModal}
            className="px-5 py-3 rounded-2xl bg-purple-600 hover:bg-purple-500 text-white font-bold text-xs shadow-lg shadow-purple-600/30 transition-all flex items-center justify-center gap-2"
          >
            <Plus className="w-4 h-4" />
            <span>Nueva Suscripción</span>
          </button>
        </div>
      </div>

      {/* Subscriptions Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {subscriptions.map(sub => {
          const card = cards.find(c => c.id === sub.cardId);

          return (
            <div
              key={sub.id}
              className={`p-5 rounded-3xl bg-white dark:bg-slate-900 border transition-all flex flex-col justify-between space-y-4 shadow-sm ${
                sub.isActive
                  ? 'border-slate-200 dark:border-slate-800'
                  : 'border-slate-200 dark:border-slate-800 opacity-60'
              }`}
            >
              <div>
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs font-bold uppercase tracking-wider px-2.5 py-0.5 rounded-full bg-purple-100 text-purple-800 dark:bg-purple-900/50 dark:text-purple-300">
                        {sub.periodicity}
                      </span>
                      <span className="text-xs text-slate-500 dark:text-slate-400">
                        {card?.name || 'TDC'}
                      </span>
                    </div>
                    <h4 className="text-base font-extrabold text-slate-900 dark:text-white">
                      {sub.name}
                    </h4>
                    <span className="text-xs text-slate-400">
                      Cobro el día <strong className="text-slate-700 dark:text-slate-300">{sub.billingDayOfMonth}</strong> de cada mes
                    </span>
                  </div>

                  <div className="text-right">
                    <span className="text-lg font-black font-mono text-slate-900 dark:text-white">
                      {formatCurrency(sub.totalMonthlyAmount, isPrivateMode)}
                    </span>
                  </div>
                </div>

                {/* Participants breakdown */}
                {sub.participantsSummary && (
                  <div className="mt-3 p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 flex items-start gap-2 text-xs text-slate-600 dark:text-slate-300">
                    <Users className="w-3.5 h-3.5 text-purple-600 dark:text-purple-400 mt-0.5 shrink-0" />
                    <div>
                      <span className="font-semibold block text-[11px] text-slate-400 uppercase">
                        Participantes & División:
                      </span>
                      <span>{sub.participantsSummary}</span>
                    </div>
                  </div>
                )}
              </div>

              {/* Action Buttons */}
              <div className="pt-2 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
                <button
                  onClick={() => recordSubAsExpense(sub)}
                  className="text-xs font-bold text-blue-600 dark:text-blue-400 hover:underline flex items-center gap-1"
                >
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  <span>Cargar a tarjeta</span>
                </button>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => updateSubscription({ ...sub, isActive: !sub.isActive })}
                    className={`px-2.5 py-1 rounded-lg text-xs font-semibold ${
                      sub.isActive
                        ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300'
                        : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400'
                    }`}
                  >
                    {sub.isActive ? 'Activa' : 'Pausada'}
                  </button>
                  <button
                    onClick={() => openEditModal(sub)}
                    className="p-1.5 rounded-lg text-slate-500 hover:text-slate-900 hover:bg-slate-100 dark:hover:bg-slate-800"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => {
                      if (confirm(`¿Eliminar la suscripción "${sub.name}"?`)) {
                        deleteSubscription(sub.id);
                      }
                    }}
                    className="p-1.5 rounded-lg text-red-500 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950/40"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Add / Edit Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm overflow-y-auto">
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl max-w-lg w-full shadow-2xl my-6 overflow-hidden">
            <div className="p-5 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between bg-purple-50/70 dark:bg-purple-950/30">
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                {subToEdit ? 'Editar Suscripción' : 'Nueva Suscripción Fija'}
              </h3>
              <button onClick={() => setIsAddModalOpen(false)}>
                <X className="w-5 h-5 text-slate-400" />
              </button>
            </div>

            <form onSubmit={handleSave} className="p-5 space-y-4">
              <div>
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                  Nombre del Servicio
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ej: Netflix, Spotify, iCloud, ChatGPT"
                  value={name}
                  onChange={e => setName(e.target.value)}
                  className="w-full px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-medium"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Tarjeta Domiciliada
                  </label>
                  <select
                    value={cardId}
                    onChange={e => setCardId(Number(e.target.value))}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-medium"
                  >
                    {cards.map(c => (
                      <option key={c.id} value={c.id}>
                        {c.name} ({c.bank})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Día de Cobro (1-31)
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="31"
                    required
                    value={billingDayOfMonth}
                    onChange={e => setBillingDayOfMonth(Number(e.target.value))}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-medium"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Monto Cobrado ($)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    value={totalMonthlyAmount}
                    onChange={e => setTotalMonthlyAmount(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-bold font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Periodicidad
                  </label>
                  <select
                    value={periodicity}
                    onChange={e => setPeriodicity(e.target.value as Subscription['periodicity'])}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-medium"
                  >
                    <option value="MENSUAL">Mensual</option>
                    <option value="BIMESTRAL">Bimestral</option>
                    <option value="TRIMESTRAL">Trimestral</option>
                    <option value="SEMESTRAL">Semestral</option>
                    <option value="ANUAL">Anual</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                  División de Gastos / Participantes
                </label>
                <input
                  type="text"
                  placeholder="Ej: Ale ($150) / Personal ($149)"
                  value={participantsSummary}
                  onChange={e => setParticipantsSummary(e.target.value)}
                  className="w-full px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-medium"
                />
              </div>

              <div className="pt-2 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsAddModalOpen(false)}
                  className="px-4 py-2 rounded-xl border border-slate-200 text-xs font-semibold"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-purple-600 text-white text-xs font-bold shadow-md"
                >
                  Guardar Suscripción
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
