import React, { useRef } from 'react';
import { useApp } from '../context/AppContext.tsx';
import {
  ShieldCheck,
  Download,
  Upload,
  RefreshCw,
  Eye,
  EyeOff,
  Sun,
  Moon,
  Info,
  Database,
  CheckCircle,
} from 'lucide-react';

export const AccountScreen: React.FC = () => {
  const {
    cards,
    expenses,
    payments,
    subscriptions,
    fuelEntries,
    serviceEntries,
    isDarkMode,
    toggleDarkMode,
    isPrivateMode,
    togglePrivateMode,
    resetToSampleData,
    exportDataJson,
    importDataJson,
  } = useApp();

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleExport = () => {
    const jsonStr = exportDataJson();
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `control_tdc_backup_${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = event => {
      const content = event.target?.result as string;
      const success = importDataJson(content);
      if (success) {
        alert('¡Datos restaurados con éxito desde el archivo de respaldo!');
      } else {
        alert('El archivo no tiene un formato de respaldo válido de Control TDC.');
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="space-y-6 pb-12 max-w-4xl mx-auto">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-black text-slate-900 dark:text-white">
          Ajustes y Respaldo de Datos
        </h2>
        <p className="text-xs text-slate-500 dark:text-slate-400">
          Personaliza tu experiencia, privacidad y resguarda toda tu información
        </p>
      </div>

      {/* Preferences Section */}
      <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
        <h3 className="text-base font-extrabold text-slate-900 dark:text-white">
          Preferencias de la Aplicación
        </h3>

        {/* Privacy Mode Toggle */}
        <div className="flex items-center justify-between py-2 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-blue-50 dark:bg-blue-950 text-blue-600 dark:text-blue-400">
              {isPrivateMode ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
            </div>
            <div>
              <span className="font-bold text-sm text-slate-900 dark:text-white block">
                Modo Privacidad
              </span>
              <span className="text-xs text-slate-400">
                Oculta los montos y saldos con asteriscos (••••) en lugares públicos
              </span>
            </div>
          </div>
          <button
            onClick={togglePrivateMode}
            className={`w-12 h-6 flex items-center rounded-full p-1 transition-colors ${
              isPrivateMode ? 'bg-blue-600' : 'bg-slate-300 dark:bg-slate-700'
            }`}
          >
            <div
              className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                isPrivateMode ? 'translate-x-6' : 'translate-x-0'
              }`}
            />
          </button>
        </div>

        {/* Dark Mode Toggle */}
        <div className="flex items-center justify-between py-2">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-purple-50 dark:bg-purple-950 text-purple-600 dark:text-purple-400">
              {isDarkMode ? <Moon className="w-5 h-5" /> : <Sun className="w-5 h-5" />}
            </div>
            <div>
              <span className="font-bold text-sm text-slate-900 dark:text-white block">
                Tema Oscuro
              </span>
              <span className="text-xs text-slate-400">
                Atenúa el brillo y ahorra batería con el modo de contraste oscuro
              </span>
            </div>
          </div>
          <button
            onClick={toggleDarkMode}
            className={`w-12 h-6 flex items-center rounded-full p-1 transition-colors ${
              isDarkMode ? 'bg-purple-600' : 'bg-slate-300 dark:bg-slate-700'
            }`}
          >
            <div
              className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                isDarkMode ? 'translate-x-6' : 'translate-x-0'
              }`}
            />
          </button>
        </div>
      </div>

      {/* Backup and Restore */}
      <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
        <div className="flex items-center gap-2">
          <Database className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
          <h3 className="text-base font-extrabold text-slate-900 dark:text-white">
            Respaldo de Base de Datos Local
          </h3>
        </div>

        <p className="text-xs text-slate-500 dark:text-slate-400">
          Tus datos se guardan de forma privada y encriptada en tu dispositivo (navegador). Puedes exportar un archivo JSON para llevarlo a otro dispositivo o respaldar periódicamente.
        </p>

        {/* Stats Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-2 pt-1 text-center text-xs">
          <div className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
            <span className="text-slate-400 block text-[10px] font-bold">Tarjetas</span>
            <span className="text-base font-black text-slate-900 dark:text-white">{cards.length}</span>
          </div>
          <div className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
            <span className="text-slate-400 block text-[10px] font-bold">Gastos</span>
            <span className="text-base font-black text-slate-900 dark:text-white">{expenses.length}</span>
          </div>
          <div className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
            <span className="text-slate-400 block text-[10px] font-bold">Abonos</span>
            <span className="text-base font-black text-slate-900 dark:text-white">{payments.length}</span>
          </div>
          <div className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
            <span className="text-slate-400 block text-[10px] font-bold">Suscripciones</span>
            <span className="text-base font-black text-slate-900 dark:text-white">{subscriptions.length}</span>
          </div>
          <div className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 col-span-2 sm:col-span-1">
            <span className="text-slate-400 block text-[10px] font-bold">Gasolina + Serv.</span>
            <span className="text-base font-black text-slate-900 dark:text-white">
              {fuelEntries.length + serviceEntries.length}
            </span>
          </div>
        </div>

        {/* Buttons */}
        <div className="pt-3 flex flex-wrap gap-3">
          <button
            onClick={handleExport}
            className="px-4 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-md transition-colors flex items-center gap-1.5"
          >
            <Download className="w-4 h-4" />
            <span>Exportar Respaldo JSON</span>
          </button>

          <button
            onClick={() => fileInputRef.current?.click()}
            className="px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 font-bold text-xs text-slate-700 dark:text-slate-300 transition-colors flex items-center gap-1.5"
          >
            <Upload className="w-4 h-4" />
            <span>Restaurar desde JSON</span>
          </button>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept=".json,application/json"
            className="hidden"
          />

          <button
            onClick={() => {
              if (confirm('¿Restablecer los datos de ejemplo predeterminados? Se sobreescribirán los registros actuales.')) {
                resetToSampleData();
              }
            }}
            className="px-4 py-2.5 rounded-xl text-red-600 hover:bg-red-50 dark:hover:bg-red-950/40 text-xs font-bold transition-colors ml-auto flex items-center gap-1"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Restablecer Datos de Muestra</span>
          </button>
        </div>
      </div>

      {/* Mexican Banking Regulations Info Card */}
      <div className="p-6 rounded-3xl bg-slate-50 dark:bg-slate-850 border border-slate-200 dark:border-slate-850 space-y-3">
        <div className="flex items-center gap-2">
          <ShieldCheck className="w-5 h-5 text-emerald-600" />
          <h4 className="text-sm font-bold text-slate-900 dark:text-white">
            Cumplimiento Regulatorio Mexicano (Banxico & Condusef)
          </h4>
        </div>
        <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">
          Control TDC implementa el <strong>Artículo 23 de la Ley para la Transparencia y Ordenamiento de los Servicios Financieros (LTOPSF)</strong>. Si tu fecha límite de pago coincide con un sábado, domingo o día inhábil bancario oficial determinado por la Comisión Nacional Bancaria y de Valores (CNBV), el sistema recorre automáticamente la fecha límite al día hábil siguiente sin cobro de intereses moratorios ni comisiones.
        </p>
        <p className="text-xs text-slate-500 dark:text-slate-400">
          Versión Web 2.4.0 • Migración completa desde Control TDC Android
        </p>
      </div>
    </div>
  );
};
