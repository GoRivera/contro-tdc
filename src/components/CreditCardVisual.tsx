import React from 'react';
import { CreditCard } from '../types.ts';
import { matchRealCardBranding } from '../domain/CardBrandingEngine.ts';
import { Wifi, Sparkles } from 'lucide-react';

interface CreditCardVisualProps {
  card: CreditCard;
  className?: string;
  onClick?: () => void;
  showDetails?: boolean;
}

export const CreditCardVisual: React.FC<CreditCardVisualProps> = ({
  card,
  className = '',
  onClick,
  showDetails = true,
}) => {
  const branding = matchRealCardBranding(card.name, card.bank, card.network);

  // Background styling based on finish type and brand colors
  const getCardBackgroundStyle = (): React.CSSProperties => {
    const p = branding.primaryColorHex;
    const s = branding.secondaryColorHex;

    switch (branding.finishType) {
      case 'LUXURY_GOLD':
        return {
          background: `linear-gradient(135deg, ${p} 0%, #D4AF37 35%, ${s} 70%, #8C6D1F 100%)`,
        };
      case 'METALLIC_BRUSHED':
        return {
          background: `linear-gradient(135deg, ${p} 0%, #3a4454 45%, ${s} 85%)`,
        };
      case 'NEON_PURPLE':
        return {
          background: `linear-gradient(135deg, ${p} 0%, #6d08b3 50%, ${s} 100%)`,
        };
      case 'CARBON_SLATE':
        return {
          background: `linear-gradient(135deg, #1f2327 0%, #0d0e10 60%, ${p} 100%)`,
        };
      case 'CENTURION_STEEL':
        return {
          background: `linear-gradient(135deg, #71717a 0%, #3f3f46 50%, #18181b 100%)`,
        };
      case 'GLOSSY_VIBRANT':
      default:
        return {
          background: `linear-gradient(135deg, ${p} 0%, ${s} 100%)`,
        };
    }
  };

  return (
    <div
      onClick={onClick}
      id={`card-visual-${card.id}`}
      style={getCardBackgroundStyle()}
      className={`relative overflow-hidden rounded-2xl p-5 text-white shadow-xl shadow-slate-900/20 border border-white/15 transition-all duration-300 select-none ${
        onClick ? 'cursor-pointer hover:scale-[1.02] active:scale-[0.99]' : ''
      } ${className}`}
    >
      {/* Texture overlay for luxury and metallic cards */}
      <div className="absolute inset-0 opacity-15 pointer-events-none bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:16px_16px]" />
      
      {/* Diagonal gloss reflection line */}
      <div className="absolute -top-24 -right-24 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />

      {/* Top row: Bank name & Network / Type */}
      <div className="relative z-10 flex items-center justify-between mb-4">
        <div>
          <span className="text-xs font-semibold tracking-wider uppercase opacity-80">
            {branding.bankDisplayName}
          </span>
          <h3 className="text-base font-extrabold tracking-wide drop-shadow-sm">
            {card.name}
          </h3>
        </div>
        <div className="flex items-center gap-2">
          {card.isDepartmental && (
            <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-black/25 backdrop-blur-sm border border-white/20">
              Departamental
            </span>
          )}
          <span className="text-sm font-black tracking-wider uppercase drop-shadow">
            {card.network}
          </span>
        </div>
      </div>

      {/* Middle row: EMV Chip & Contactless Wifi */}
      <div className="relative z-10 flex items-center gap-3 my-2">
        {/* EMV Microchip */}
        <div className="w-10 h-7 rounded-md bg-gradient-to-tr from-amber-300 via-yellow-200 to-amber-400 border border-amber-600/40 shadow-inner flex items-center justify-center relative overflow-hidden">
          <div className="w-full h-[1px] bg-amber-700/40 absolute top-2" />
          <div className="w-full h-[1px] bg-amber-700/40 absolute bottom-2" />
          <div className="h-full w-[1px] bg-amber-700/40 absolute left-3" />
          <div className="h-full w-[1px] bg-amber-700/40 absolute right-3" />
          <div className="w-2.5 h-2.5 rounded-sm border border-amber-800/40" />
        </div>

        {/* Contactless waves */}
        <Wifi className="w-4 h-4 text-white/70 rotate-90" />
      </div>

      {/* Card number representation */}
      <div className="relative z-10 font-mono text-base tracking-[0.22em] font-semibold text-white/95 my-3 drop-shadow">
        <span>•••• •••• •••• {card.last4Digits || '0000'}</span>
      </div>

      {/* Bottom row: Cardholder Name & Cutoff/Payment dates */}
      <div className="relative z-10 flex items-end justify-between text-xs pt-1 border-t border-white/15">
        <div>
          <span className="text-[9px] uppercase tracking-wider block opacity-70">
            Titular
          </span>
          <span className="font-semibold tracking-wide uppercase">
            {card.cardholderName || 'USUARIO'}
          </span>
        </div>

        {showDetails && (
          <div className="text-right flex items-center gap-3">
            <div>
              <span className="text-[9px] uppercase tracking-wider block opacity-70">
                Corte
              </span>
              <span className="font-bold">Día {card.cutoffDay}</span>
            </div>
            <div className="border-l border-white/20 pl-2">
              <span className="text-[9px] uppercase tracking-wider block opacity-70">
                Límite Pago
              </span>
              <span className="font-bold text-amber-200">Día {card.paymentDueDay}</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
