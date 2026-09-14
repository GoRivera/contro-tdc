import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext.tsx';
import { ServiceEntry } from '../types.ts';
import { formatCurrency, formatDate } from '../domain/CreditCardCalculator.ts';
import {
  Zap,
  Droplet,
  Flame,
  Wifi,
  Plus,
  Trash2,
  Calendar,
  X,
  CreditCard as CardIcon,
  TrendingUp,
} from 'lucide-react';

export const ServicesScreen: React.FC = () => {
  const { cards, serviceEntries, addServiceEntry, deleteServiceEntry, addExpense, isPrivateMode } = useApp();

  const [isAddModalOpen, setIsAddModalOpen] = useState<boolean>(false);

  // Form states
  const [serviceType, setServiceType] = useState<ServiceEntry['serviceType']>('Luz (CFE)');
  const [cardId, setCardId] = useState<number>(cards[0]?.id || 1);
  const [amount, setAmount] = useState<string>('850');
  const [unitsConsumed, setUnitsConsumed] = useState<string>('240');
  const [unitName, setUnitName] = useState<string>('kWh');
  const [billingPeriod, setBillingPeriod] = useState<string>('Bimestre Julio - Agosto 2026');
  const [notes, setNotes] = useState<string>('');

  const totalSpent = useMemo(
    () => serviceEntries.reduce((sum: number, s: ServiceEntry) => sum + s.amount, 0),
    [serviceEntries]
  );

  const handleTypeChange = (type: ServiceEntry['serviceType']) => {
    setServiceType(type);
    if (type === 'Luz (CFE)') setUnitName('kWh');
    else if (type === 'Agua') setUnitName('m³');
    else if (type === 'Gas') setUnitName('L');
    else if (type === 'Internet') setUnitName('Mes');
    else setUnitName('Servicio');
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const parsedAmount = parseFloat(amount);
    const parsedUnits = parseFloat(unitsConsumed) || 0;
    if (isNaN(parsedAmount) || parsedAmount <= 0) return;

    const unitCost = parsedUnits > 0 ? Math.round((parsedAmount / parsedUnits) * 100) / 100 : 0;

    addServiceEntry({
      serviceType,
      cardId,
      amount: parsedAmount,
      unitsConsumed: parsedUnits,
      unitName,
      costPerUnit: unitCost,
      billingPeriod: billingPeriod.trim(),
      dateMillis: Date.now(),
      notes: notes.trim(),
    });

    // Also register expense on card
    addExpense({
      cardId,
      concept: `Servicio ${serviceType}`,
      amount: parsedAmount,
      dateMillis: Date.now(),
      beneficiary: 'Hogar',
      category: 'Servicios',
      isMsi: false,
      msiTotalMonths: 1,
      msiCurrentInstallment: 1,
      msiTotalPurchaseAmount: 0,
      notes: `${billingPeriod} • ${parsedUnits} ${unitName}`,
      targetStatementMonth: 'Septiembre 2026',
    });

    setIsAddModalOpen(false);
  };

  const getServiceIcon = (type: ServiceEntry['serviceType']) => {
    switch (type) {
      case 'Luz (CFE)':
        return <Zap className="w-5 h-5 text-amber-500" />;
      case 'Agua':
        return <Droplet className="w-5 h-5 text-cyan-500" />;
      case 'Gas':
        return <Flame className="w-5 h-5 text-rose-500" />;
      case 'Internet':
        return <Wifi className="w-5 h-5 text-blue-500" />;
      default:
        return <Zap className="w-5 h-5 text-indigo-500" />;
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Hero KPI Card */}
      <div className="bg-gradient-to-br from-cyan-950 via-slate-900 to-blue-950 text-white rounded-3xl p-6 shadow-xl border border-cyan-500/20 relative overflow-hidden">
        <div className="relative z-10 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1.5">
              <Zap className="w-4 h-4 text-cyan-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-cyan-300">
                Gasto Total en Servicios del Hogar
              </span>
            </div>
            <div className="text-3xl sm:text-4xl font-black font-mono tracking-tight text-white">
              {formatCurrency(totalSpent, isPrivateMode)}
            </div>
            <p className="text-xs text-cyan-200/80 mt-1">
              {serviceEntries.length} recibos y facturas pagadas con tarjeta
            </p>
          </div>

          <button
            onClick={() => setIsAddModalOpen(true)}
            className="px-5 py-3 rounded-2xl bg-cyan-600 hover:bg-cyan-500 text-white font-bold text-xs shadow-lg shadow-cyan-600/30 transition-all flex items-center justify-center gap-2"
          >
            <Plus className="w-4 h-4" />
            <span>Registrar Recibo / Servicio</span>
          </button>
        </div>
      </div>

      {/* Services List */}
      <div className="space-y-3">
        <h3 className="text-base font-extrabold text-slate-900 dark:text-white">
          Recibos y Consumos Registrados
        </h3>

        {serviceEntries.length === 0 ? (
          <div className="p-10 text-center rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-400 text-xs">
            No tienes recibos de servicios registrados.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {serviceEntries.map((entry: ServiceEntry) => {
              const card = cards.find(c => c.id === entry.cardId);

              return (
                <div
                  key={entry.id}
                  className="p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between space-y-3"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex items-start gap-3">
                      <div className="p-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 shrink-0">
                        {getServiceIcon(entry.serviceType)}
                      </div>
                      <div>
                        <div className="flex items-center gap-2 mb-0.5">
                          <h4 className="text-base font-extrabold text-slate-900 dark:text-white">
                            {entry.serviceType}
                          </h4>
                          <span className="text-[10px] text-slate-400 font-medium">
                            {formatDate(entry.dateMillis)}
                          </span>
                        </div>
                        <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 block">
                          {entry.billingPeriod}
                        </span>
                        <span className="text-xs text-slate-400">
                          {card?.name || 'TDC'} • {entry.notes || 'Pago puntual'}
                        </span>
                      </div>
                    </div>

                    <div className="text-right">
                      <span className="text-lg font-black font-mono text-slate-900 dark:text-white">
                        {formatCurrency(entry.amount, isPrivateMode)}
                      </span>
                      {(entry.unitsConsumed ?? 0) > 0 && (
                        <span className="text-xs text-slate-400 block">
                          {entry.unitsConsumed} {entry.unitName}
                        </span>
                      )}
                    </div>
                  </div>

                  {(entry.unitsConsumed ?? 0) > 0 && (entry.costPerUnit ?? 0) > 0 && (
                    <div className="p-2 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 text-xs flex items-center justify-between">
                      <span className="text-slate-500 font-medium">
                        Costo unitario ({entry.unitName}):
                      </span>
                      <span className="font-bold text-slate-900 dark:text-white font-mono">
                        ${(entry.costPerUnit ?? 0).toFixed(2)} / {entry.unitName}
                      </span>
                    </div>
                  )}

                  <div className="pt-2 border-t border-slate-100 dark:border-slate-800 flex justify-end">
                    <button
                      onClick={() => {
                        if (confirm('¿Eliminar este registro de servicio?')) {
                          deleteServiceEntry(entry.id);
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

      {/* Add Service Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm overflow-y-auto">
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl max-w-lg w-full shadow-2xl my-6 overflow-hidden">
            <div className="p-5 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between bg-cyan-50/70 dark:bg-cyan-950/30">
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                Registrar Pago de Servicio
              </h3>
              <button onClick={() => setIsAddModalOpen(false)}>
                <X className="w-5 h-5 text-slate-400" />
              </button>
            </div>

            <form onSubmit={handleSave} className="p-5 space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Tipo de Servicio
                  </label>
                  <select
                    value={serviceType}
                    onChange={e => handleTypeChange(e.target.value as ServiceEntry['serviceType'])}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-medium"
                  >
                    <option value="Luz (CFE)">Luz (CFE)</option>
                    <option value="Agua">Agua Potable</option>
                    <option value="Gas">Gas LP / Natural</option>
                    <option value="Internet">Internet / Telefonía</option>
                    <option value="Otro">Otro Servicio</option>
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
                <div className="col-span-1">
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Monto Total ($)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    placeholder="Ej: 850"
                    value={amount}
                    onChange={e => setAmount(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-bold font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Consumo ({unitName})
                  </label>
                  <input
                    type="number"
                    step="0.1"
                    placeholder="Ej: 240"
                    value={unitsConsumed}
                    onChange={e => setUnitsConsumed(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-bold font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Unidad
                  </label>
                  <input
                    type="text"
                    value={unitName}
                    onChange={e => setUnitName(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-medium"
                  />
                </div>
              </div>

              <div>
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                  Periodo de Facturación
                </label>
                <input
                  type="text"
                  placeholder="Ej: Bimestre Julio - Agosto 2026"
                  value={billingPeriod}
                  onChange={e => setBillingPeriod(e.target.value)}
                  className="w-full px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-medium"
                />
              </div>

              <div>
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                  Notas
                </label>
                <input
                  type="text"
                  placeholder="Ej: Número de medidor, tarifa DAC, etc."
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
                  className="px-5 py-2 rounded-xl bg-cyan-600 text-white text-xs font-bold shadow-md"
                >
                  Guardar Recibo
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
