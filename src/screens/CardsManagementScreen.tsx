import React, { useState } from 'react';
import { useApp } from '../context/AppContext.tsx';
import { CreditCard } from '../types.ts';
import { CreditCardVisual } from '../components/CreditCardVisual.tsx';
import { calculateCardCreditBalance, formatCurrency } from '../domain/CreditCardCalculator.ts';
import { MEXICAN_BANKS } from '../domain/MexicanBanks.ts';
import {
  Plus,
  Edit2,
  Trash2,
  CreditCard as CardIcon,
  ShieldCheck,
  Percent,
  Calendar,
  DollarSign,
  X,
  Sparkles,
} from 'lucide-react';

const POPULAR_CARD_PRESETS = [
  { name: 'BBVA Azul', bank: 'BBVA', network: 'Visa', limit: 45000, cutoff: 18, due: 8, rate: 58 },
  { name: 'Nu Tarjeta de Crédito', bank: 'Nu México', network: 'Mastercard', limit: 30000, cutoff: 14, due: 4, rate: 52 },
  { name: 'Santander LikeU', bank: 'Santander', network: 'Mastercard', limit: 35000, cutoff: 20, due: 10, rate: 55 },
  { name: 'Costco Citibanamex', bank: 'Citibanamex', network: 'Mastercard', limit: 50000, cutoff: 24, due: 14, rate: 49 },
  { name: 'Banorte Por Ti', bank: 'Banorte', network: 'Visa', limit: 40000, cutoff: 10, due: 30, rate: 57 },
  { name: 'The Platinum Card', bank: 'American Express', network: 'American Express', limit: 120000, cutoff: 28, due: 18, rate: 45 },
  { name: 'Tarjeta Liverpool', bank: 'Liverpool', network: 'Visa', limit: 25000, cutoff: 15, due: 5, rate: 62, isDepartmental: true },
];

export const CardsManagementScreen: React.FC = () => {
  const { cards, expenses, payments, addCard, updateCard, deleteCard, isPrivateMode } = useApp();

  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [cardToEdit, setCardToEdit] = useState<CreditCard | null>(null);

  // Form states
  const [name, setName] = useState<string>('');
  const [bank, setBank] = useState<string>('BBVA');
  const [network, setNetwork] = useState<CreditCard['network']>('Visa');
  const [last4Digits, setLast4Digits] = useState<string>('');
  const [cutoffDay, setCutoffDay] = useState<number>(15);
  const [paymentDueDay, setPaymentDueDay] = useState<number>(5);
  const [creditLimit, setCreditLimit] = useState<string>('30000');
  const [annualFee, setAnnualFee] = useState<string>('0');
  const [annualInterestRatePercent, setAnnualInterestRatePercent] = useState<string>('55');
  const [cardholderName, setCardholderName] = useState<string>('TITULAR');
  const [isDepartmental, setIsDepartmental] = useState<boolean>(false);

  const openAddModal = () => {
    setCardToEdit(null);
    setName('');
    setBank('BBVA');
    setNetwork('Visa');
    setLast4Digits('');
    setCutoffDay(15);
    setPaymentDueDay(5);
    setCreditLimit('30000');
    setAnnualFee('0');
    setAnnualInterestRatePercent('55');
    setCardholderName('TITULAR');
    setIsDepartmental(false);
    setIsModalOpen(true);
  };

  const openEditModal = (card: CreditCard) => {
    setCardToEdit(card);
    setName(card.name);
    setBank(card.bank);
    setNetwork(card.network);
    setLast4Digits(card.last4Digits);
    setCutoffDay(card.cutoffDay);
    setPaymentDueDay(card.paymentDueDay);
    setCreditLimit(card.creditLimit.toString());
    setAnnualFee((card.annualFee ?? 0).toString());
    setAnnualInterestRatePercent(card.annualInterestRatePercent.toString());
    setCardholderName(card.cardholderName);
    setIsDepartmental(card.isDepartmental);
    setIsModalOpen(true);
  };

  const handleApplyPreset = (preset: typeof POPULAR_CARD_PRESETS[0]) => {
    setName(preset.name);
    setBank(preset.bank);
    setNetwork(preset.network as CreditCard['network']);
    setCreditLimit(preset.limit.toString());
    setCutoffDay(preset.cutoff);
    setPaymentDueDay(preset.due);
    setAnnualInterestRatePercent(preset.rate.toString());
    setIsDepartmental(!!preset.isDepartmental);
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const limitNum = parseFloat(creditLimit) || 0;
    const feeNum = parseFloat(annualFee) || 0;
    const rateNum = parseFloat(annualInterestRatePercent) || 50;

    if (cardToEdit) {
      updateCard({
        ...cardToEdit,
        name: name.trim(),
        bank,
        network,
        last4Digits: last4Digits.slice(-4),
        cutoffDay,
        paymentDueDay,
        creditLimit: limitNum,
        annualFee: feeNum,
        annualInterestRatePercent: rateNum,
        cardholderName: cardholderName.trim().toUpperCase(),
        isDepartmental,
      });
    } else {
      addCard({
        name: name.trim(),
        bank,
        network,
        last4Digits: last4Digits.slice(-4) || '1234',
        cutoffDay,
        paymentDueDay,
        creditLimit: limitNum,
        annualFee: feeNum,
        annualInterestRatePercent: rateNum,
        primaryColorHex: '#1e3a8a',
        secondaryColorHex: '#172554',
        graceDays: 20,
        cardholderName: cardholderName.trim().toUpperCase() || 'USUARIO',
        isDepartmental,
        isActive: true,
      });
    }

    setIsModalOpen(false);
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header & Add Button */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-black text-slate-900 dark:text-white">
            Billetera de Tarjetas de Crédito
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Administra tus líneas de crédito, fechas de corte y reglas bancarias
          </p>
        </div>

        <button
          onClick={openAddModal}
          className="px-4 py-2.5 rounded-2xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-md shadow-blue-600/30 transition-all flex items-center gap-1.5"
        >
          <Plus className="w-4 h-4" />
          <span>Agregar Tarjeta</span>
        </button>
      </div>

      {/* Cards Wallet List */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {cards.map(card => {
          const balance = calculateCardCreditBalance(card, expenses, payments);
          const isHighOccupancy = balance.occupancyPercentage > 60;

          return (
            <div
              key={card.id}
              className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-5 flex flex-col justify-between"
            >
              {/* Card visual rendering */}
              <CreditCardVisual card={card} />

              {/* Credit Line & Utilization Progress */}
              <div>
                <div className="flex items-center justify-between text-xs mb-1.5 font-semibold">
                  <span className="text-slate-500">Uso de Línea de Crédito</span>
                  <span className={`font-bold font-mono ${isHighOccupancy ? 'text-amber-600 dark:text-amber-400' : 'text-slate-900 dark:text-white'}`}>
                    {balance.occupancyPercentage}% Ocupado
                  </span>
                </div>
                <div className="w-full bg-slate-100 dark:bg-slate-800 h-2.5 rounded-full overflow-hidden">
                  <div
                    className={`h-2.5 rounded-full transition-all duration-300 ${
                      balance.occupancyPercentage > 75
                        ? 'bg-red-500'
                        : balance.occupancyPercentage > 45
                        ? 'bg-amber-500'
                        : 'bg-emerald-500'
                    }`}
                    style={{ width: `${Math.min(100, balance.occupancyPercentage)}%` }}
                  />
                </div>

                <div className="grid grid-cols-3 gap-2 mt-3 text-xs">
                  <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
                    <span className="text-[10px] text-slate-400 block font-semibold">
                      Línea Total
                    </span>
                    <span className="font-mono font-bold text-slate-900 dark:text-white">
                      {formatCurrency(balance.totalCreditLimit, isPrivateMode)}
                    </span>
                  </div>
                  <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
                    <span className="text-[10px] text-slate-400 block font-semibold">
                      Ocupado
                    </span>
                    <span className="font-mono font-bold text-slate-900 dark:text-white">
                      {formatCurrency(balance.totalOccupiedCredit, isPrivateMode)}
                    </span>
                  </div>
                  <div className="p-2.5 rounded-xl bg-emerald-50/70 dark:bg-emerald-950/30 border border-emerald-100 dark:border-emerald-900/40">
                    <span className="text-[10px] text-emerald-600 dark:text-emerald-400 block font-semibold">
                      Disponible
                    </span>
                    <span className="font-mono font-bold text-emerald-700 dark:text-emerald-300">
                      {formatCurrency(balance.availableCredit, isPrivateMode)}
                    </span>
                  </div>
                </div>
              </div>

              {/* Bank Metadata Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-2 border-t border-slate-100 dark:border-slate-800 text-xs">
                <div>
                  <span className="text-[10px] text-slate-400 uppercase font-semibold block">
                    Día Corte
                  </span>
                  <span className="font-bold text-slate-800 dark:text-slate-200">
                    Día {card.cutoffDay}
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 uppercase font-semibold block">
                    Límite Pago
                  </span>
                  <span className="font-bold text-blue-600 dark:text-blue-400">
                    Día {card.paymentDueDay}
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 uppercase font-semibold block">
                    Tasa Anual
                  </span>
                  <span className="font-bold text-slate-800 dark:text-slate-200">
                    {card.annualInterestRatePercent}%
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 uppercase font-semibold block">
                    Anualidad
                  </span>
                  <span className="font-bold text-slate-800 dark:text-slate-200">
                    {(card.annualFee ?? 0) > 0 ? formatCurrency(card.annualFee!, isPrivateMode) : 'Sin costo'}
                  </span>
                </div>
              </div>

              {/* Actions */}
              <div className="pt-2 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span
                    className={`w-2 h-2 rounded-full ${card.isActive ? 'bg-emerald-500' : 'bg-slate-400'}`}
                  />
                  <span className="text-xs text-slate-500 font-medium">
                    {card.isActive ? 'Tarjeta Activa' : 'Tarjeta Inactiva'}
                  </span>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => openEditModal(card)}
                    className="p-2 rounded-xl text-slate-500 hover:text-slate-900 hover:bg-slate-100 dark:hover:bg-slate-800"
                    title="Editar configuración"
                  >
                    <Edit2 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => {
                      if (confirm(`¿Eliminar la tarjeta "${card.name}"? Los gastos asociados conservarán el registro.`)) {
                        deleteCard(card.id);
                      }
                    }}
                    className="p-2 rounded-xl text-red-500 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950/40"
                    title="Eliminar tarjeta"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Add / Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm overflow-y-auto">
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl max-w-xl w-full shadow-2xl my-6 overflow-hidden">
            <div className="p-5 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between bg-slate-50 dark:bg-slate-800/50">
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                {cardToEdit ? 'Editar Tarjeta' : 'Registrar Nueva Tarjeta'}
              </h3>
              <button onClick={() => setIsModalOpen(false)}>
                <X className="w-5 h-5 text-slate-400" />
              </button>
            </div>

            <form onSubmit={handleSave} className="p-5 space-y-4 max-h-[80vh] overflow-y-auto">
              {/* Presets Chips */}
              {!cardToEdit && (
                <div>
                  <span className="text-xs font-bold text-slate-500 block mb-1.5">
                    Plantillas Populares en México:
                  </span>
                  <div className="flex flex-wrap gap-1.5">
                    {POPULAR_CARD_PRESETS.map(p => (
                      <button
                        key={p.name}
                        type="button"
                        onClick={() => handleApplyPreset(p)}
                        className="px-2.5 py-1 rounded-lg bg-blue-50 dark:bg-blue-950/40 text-blue-700 dark:text-blue-300 text-xs font-medium hover:bg-blue-100"
                      >
                        {p.name}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {/* Name & Bank */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Nombre de la Tarjeta
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="Ej: BBVA Platino, Nu, Banorte"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-medium"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Institución Bancaria
                  </label>
                  <select
                    value={bank}
                    onChange={e => setBank(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-medium"
                  >
                    {MEXICAN_BANKS.map(b => (
                      <option key={b.name} value={b.name}>
                        {b.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Network & Last 4 */}
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Red
                  </label>
                  <select
                    value={network}
                    onChange={e => setNetwork(e.target.value as CreditCard['network'])}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-medium"
                  >
                    <option value="Visa">Visa</option>
                    <option value="Mastercard">Mastercard</option>
                    <option value="American Express">American Express</option>
                    <option value="Carnet">Carnet</option>
                  </select>
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Últimos 4 Dígitos
                  </label>
                  <input
                    type="text"
                    maxLength={4}
                    placeholder="4821"
                    value={last4Digits}
                    onChange={e => setLast4Digits(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Titular
                  </label>
                  <input
                    type="text"
                    placeholder="ALEJANDRO R"
                    value={cardholderName}
                    onChange={e => setCardholderName(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs uppercase font-medium"
                  />
                </div>
              </div>

              {/* Cutoff & Due Days */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Día de Corte (1-31)
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="31"
                    required
                    value={cutoffDay}
                    onChange={e => setCutoffDay(Number(e.target.value))}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-bold"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Día Límite de Pago (1-31)
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="31"
                    required
                    value={paymentDueDay}
                    onChange={e => setPaymentDueDay(Number(e.target.value))}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-bold"
                  />
                </div>
              </div>

              {/* Financial values */}
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Línea de Crédito ($)
                  </label>
                  <input
                    type="number"
                    step="1000"
                    required
                    value={creditLimit}
                    onChange={e => setCreditLimit(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-bold font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Tasa Anual (%)
                  </label>
                  <input
                    type="number"
                    step="0.5"
                    value={annualInterestRatePercent}
                    onChange={e => setAnnualInterestRatePercent(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-bold font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Anualidad ($)
                  </label>
                  <input
                    type="number"
                    step="10"
                    value={annualFee}
                    onChange={e => setAnnualFee(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-mono"
                  />
                </div>
              </div>

              {/* Departamental toggle */}
              <div className="flex items-center gap-2 pt-1">
                <input
                  type="checkbox"
                  id="departmental-check"
                  checked={isDepartmental}
                  onChange={e => setIsDepartmental(e.target.checked)}
                  className="w-4 h-4 accent-blue-600 rounded"
                />
                <label htmlFor="departmental-check" className="text-xs font-medium text-slate-700 dark:text-slate-300 cursor-pointer">
                  Es una tarjeta departamental (Liverpool, Palacio de Hierro, Sears, etc.)
                </label>
              </div>

              <div className="pt-3 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 rounded-xl border border-slate-200 text-xs font-semibold"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-blue-600 text-white text-xs font-bold shadow-md hover:bg-blue-700"
                >
                  Guardar Tarjeta
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
