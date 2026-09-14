import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext.tsx';
import { FuelEntry } from '../types.ts';
import { formatCurrency, formatDate } from '../domain/CreditCardCalculator.ts';
import {
  Flame,
  Plus,
  Gauge,
  TrendingUp,
  CreditCard as CardIcon,
  Users,
  Trash2,
  Calendar,
  X,
  CheckCircle2,
} from 'lucide-react';

export const FuelScreen: React.FC = () => {
  const { cards, fuelEntries, addFuelEntry, deleteFuelEntry, addExpense, isPrivateMode } = useApp();

  const [isAddModalOpen, setIsAddModalOpen] = useState<boolean>(false);

  // Form states
  const [cardId, setCardId] = useState<number>(cards[0]?.id || 1);
  const [kmDriven, setKmDriven] = useState<string>('450');
  const [fuelType, setFuelType] = useState<FuelEntry['fuelType']>('Premium (Roja)');
  const [pricePerLiter, setPricePerLiter] = useState<string>('25.50');
  const [litersLoaded, setLitersLoaded] = useState<string>('40.0');
  const [isDivided, setIsDivided] = useState<boolean>(false);
  const [dividedWith, setDividedWith] = useState<string>('Ale (50%)');
  const [dividedCount, setDividedCount] = useState<number>(2);
  const [notes, setNotes] = useState<string>('');

  // Total stats
  const totalCost = useMemo(() => fuelEntries.reduce((sum, f) => sum + f.totalCost, 0), [fuelEntries]);
  const totalKm = useMemo(() => fuelEntries.reduce((sum, f) => sum + f.kmDriven, 0), [fuelEntries]);
  const totalLiters = useMemo(() => fuelEntries.reduce((sum, f) => sum + f.litersLoaded, 0), [fuelEntries]);

  const avgEfficiency = totalLiters > 0 ? (totalKm / totalLiters).toFixed(2) : '0.00';
  const avgCostPerKm = totalKm > 0 ? (totalCost / totalKm).toFixed(2) : '0.00';

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const km = parseFloat(kmDriven);
    const price = parseFloat(pricePerLiter);
    const liters = parseFloat(litersLoaded);
    if (isNaN(km) || isNaN(price) || isNaN(liters) || liters <= 0) return;

    const calcTotalCost = Math.round(price * liters * 100) / 100;
    const efficiency = km > 0 ? Math.round((km / liters) * 100) / 100 : 0;
    const personal = isDivided && dividedCount > 1 ? Math.round((calcTotalCost / dividedCount) * 100) / 100 : calcTotalCost;

    addFuelEntry({
      cardId,
      kmDriven: km,
      fuelType,
      pricePerLiter: price,
      litersLoaded: liters,
      totalCost: calcTotalCost,
      efficiencyKmPerL: efficiency,
      isDivided,
      personalShare: personal,
      dividedWith: isDivided ? dividedWith : '',
      dividedCount: isDivided ? dividedCount : 1,
      dateMillis: Date.now(),
      notes: notes.trim(),
    });

    // Optionally also register expense to credit card so it affects statements
    addExpense({
      cardId,
      concept: `Gasolina ${fuelType}`,
      amount: calcTotalCost,
      dateMillis: Date.now(),
      beneficiary: isDivided ? 'Compartido' : 'Personal',
      category: 'Gasolina',
      isMsi: false,
      msiTotalMonths: 1,
      msiCurrentInstallment: 1,
      msiTotalPurchaseAmount: 0,
      notes: `${liters}L @ $${price}/L • ${notes}`,
      targetStatementMonth: 'Septiembre 2026',
    });

    setIsAddModalOpen(false);
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Hero KPI Card */}
      <div className="bg-gradient-to-br from-amber-950 via-slate-900 to-red-950 text-white rounded-3xl p-6 shadow-xl border border-amber-500/20 relative overflow-hidden">
        <div className="relative z-10 grid grid-cols-2 lg:grid-cols-4 gap-4 items-center">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Flame className="w-4 h-4 text-amber-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-amber-300">
                Gasto en Gasolina
              </span>
            </div>
            <div className="text-2xl sm:text-3xl font-black font-mono tracking-tight text-white">
              {formatCurrency(totalCost, isPrivateMode)}
            </div>
            <span className="text-xs text-amber-200/80 mt-1 block">
              {totalLiters.toFixed(1)} Litros cargados
            </span>
          </div>

          <div>
            <div className="flex items-center gap-2 mb-1">
              <Gauge className="w-4 h-4 text-emerald-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-slate-300">
                Rendimiento Promedio
              </span>
            </div>
            <div className="text-2xl sm:text-3xl font-black font-mono tracking-tight text-emerald-400">
              {avgEfficiency} <span className="text-sm font-normal text-slate-300">km/L</span>
            </div>
            <span className="text-xs text-slate-400 mt-1 block">
              {totalKm.toLocaleString()} km totales
            </span>
          </div>

          <div>
            <div className="flex items-center gap-2 mb-1">
              <TrendingUp className="w-4 h-4 text-blue-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-slate-300">
                Costo por Kilómetro
              </span>
            </div>
            <div className="text-2xl sm:text-3xl font-black font-mono tracking-tight text-blue-400">
              ${avgCostPerKm} <span className="text-sm font-normal text-slate-300">/km</span>
            </div>
            <span className="text-xs text-slate-400 mt-1 block">
              Eficiencia de combustible
            </span>
          </div>

          <div className="col-span-2 lg:col-span-1 flex justify-start lg:justify-end">
            <button
              onClick={() => setIsAddModalOpen(true)}
              className="w-full lg:w-auto px-5 py-3 rounded-2xl bg-amber-600 hover:bg-amber-500 text-white font-bold text-xs shadow-lg shadow-amber-600/30 transition-all flex items-center justify-center gap-2"
            >
              <Plus className="w-4 h-4" />
              <span>Registrar Carga</span>
            </button>
          </div>
        </div>
      </div>

      {/* Fuel Entries List */}
      <div className="space-y-3">
        <h3 className="text-base font-extrabold text-slate-900 dark:text-white">
          Historial de Cargas de Combustible
        </h3>

        {fuelEntries.length === 0 ? (
          <div className="p-10 text-center rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-400 text-xs">
            No tienes cargas de gasolina registradas.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {fuelEntries.map(entry => {
              const card = cards.find(c => c.id === entry.cardId);
              const isPremium = entry.fuelType.includes('Roja') || entry.fuelType.includes('Premium');

              return (
                <div
                  key={entry.id}
                  className="p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between space-y-3"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <span
                          className={`text-[10px] font-bold uppercase tracking-wider px-2.5 py-0.5 rounded-full ${
                            isPremium
                              ? 'bg-red-100 text-red-800 dark:bg-red-950/60 dark:text-red-300'
                              : 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300'
                          }`}
                        >
                          {entry.fuelType}
                        </span>
                        <span className="text-xs text-slate-400">
                          {formatDate(entry.dateMillis)}
                        </span>
                      </div>
                      <h4 className="text-base font-extrabold text-slate-900 dark:text-white">
                        {entry.kmDriven} km recorridos
                      </h4>
                      <span className="text-xs text-slate-500 dark:text-slate-400">
                        {card?.name || 'TDC'} • {entry.notes || 'Carga regular'}
                      </span>
                    </div>

                    <div className="text-right">
                      <span className="text-lg font-black font-mono text-slate-900 dark:text-white">
                        {formatCurrency(entry.totalCost, isPrivateMode)}
                      </span>
                      <span className="text-xs text-slate-400 block">
                        {entry.litersLoaded} L @ ${entry.pricePerLiter}/L
                      </span>
                    </div>
                  </div>

                  {/* Stats Badges */}
                  <div className="grid grid-cols-2 gap-2 p-2.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 text-xs">
                    <div>
                      <span className="text-[10px] text-slate-400 block font-semibold">
                        Rendimiento
                      </span>
                      <span className="font-extrabold text-emerald-600 dark:text-emerald-400">
                        {entry.efficiencyKmPerL} km/L
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] text-slate-400 block font-semibold">
                        Costo / km
                      </span>
                      <span className="font-extrabold text-blue-600 dark:text-blue-400">
                        ${(entry.totalCost / (entry.kmDriven || 1)).toFixed(2)} /km
                      </span>
                    </div>
                  </div>

                  {/* Divided Tag */}
                  {entry.isDivided && (
                    <div className="p-2 rounded-xl bg-amber-50 dark:bg-amber-950/40 text-amber-800 dark:text-amber-300 text-xs flex items-center justify-between font-medium">
                      <span className="flex items-center gap-1.5">
                        <Users className="w-3.5 h-3.5 text-amber-600" />
                        <span>Dividido: {entry.dividedWith}</span>
                      </span>
                      <span className="font-bold">
                        Tu parte: {formatCurrency(entry.personalShare, isPrivateMode)}
                      </span>
                    </div>
                  )}

                  <div className="pt-2 border-t border-slate-100 dark:border-slate-800 flex justify-end">
                    <button
                      onClick={() => {
                        if (confirm('¿Eliminar este registro de gasolina?')) {
                          deleteFuelEntry(entry.id);
                        }
                      }}
                      className="p-1 text-slate-400 hover:text-red-500"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Add Fuel Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm overflow-y-auto">
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl max-w-lg w-full shadow-2xl my-6 overflow-hidden">
            <div className="p-5 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between bg-amber-50/70 dark:bg-amber-950/30">
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                Registrar Carga de Gasolina
              </h3>
              <button onClick={() => setIsAddModalOpen(false)}>
                <X className="w-5 h-5 text-slate-400" />
              </button>
            </div>

            <form onSubmit={handleSave} className="p-5 space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Tipo de Combustible
                  </label>
                  <select
                    value={fuelType}
                    onChange={e => setFuelType(e.target.value as FuelEntry['fuelType'])}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-medium"
                  >
                    <option value="Premium (Roja)">Premium (Roja)</option>
                    <option value="Regular (Verde)">Regular (Verde)</option>
                  </select>
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Tarjeta Pagada
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
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Km Recorridos
                  </label>
                  <input
                    type="number"
                    step="1"
                    required
                    placeholder="Ej: 480"
                    value={kmDriven}
                    onChange={e => setKmDriven(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-bold font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Litros Cargados
                  </label>
                  <input
                    type="number"
                    step="0.1"
                    required
                    placeholder="Ej: 42.5"
                    value={litersLoaded}
                    onChange={e => setLitersLoaded(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-bold font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Precio / Litro ($)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    placeholder="Ej: 25.80"
                    value={pricePerLiter}
                    onChange={e => setPricePerLiter(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-bold font-mono"
                  />
                </div>
              </div>

              {/* Divided toggle */}
              <div className="p-3.5 rounded-2xl bg-amber-50/70 dark:bg-amber-950/30 border border-amber-200/80 dark:border-amber-800/60">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-bold text-slate-900 dark:text-white">
                    Dividir costo del viaje / tanque
                  </span>
                  <input
                    type="checkbox"
                    checked={isDivided}
                    onChange={e => setIsDivided(e.target.checked)}
                    className="w-4 h-4 accent-amber-600 rounded"
                  />
                </div>
                {isDivided && (
                  <div className="grid grid-cols-2 gap-2 pt-2 border-t border-amber-200/60">
                    <div>
                      <label className="text-[11px] font-semibold text-slate-600 dark:text-slate-400 block mb-1">
                        Número de personas
                      </label>
                      <input
                        type="number"
                        min="2"
                        value={dividedCount}
                        onChange={e => setDividedCount(parseInt(e.target.value, 10) || 2)}
                        className="w-full px-2.5 py-1.5 rounded-lg border text-xs font-bold"
                      />
                    </div>
                    <div>
                      <label className="text-[11px] font-semibold text-slate-600 dark:text-slate-400 block mb-1">
                        Dividido con:
                      </label>
                      <input
                        type="text"
                        value={dividedWith}
                        onChange={e => setDividedWith(e.target.value)}
                        className="w-full px-2.5 py-1.5 rounded-lg border text-xs"
                      />
                    </div>
                  </div>
                )}
              </div>

              <div>
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                  Notas (Estación, viaje, etc.)
                </label>
                <input
                  type="text"
                  placeholder="Ej: Gasolinera Shell Insurgentes, viaje a Cuernavaca"
                  value={notes}
                  onChange={e => setNotes(e.target.value)}
                  className="w-full px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs"
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
                  className="px-5 py-2 rounded-xl bg-amber-600 text-white text-xs font-bold shadow-md"
                >
                  Guardar Carga
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
