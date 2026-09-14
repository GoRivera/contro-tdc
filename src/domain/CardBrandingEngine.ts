export type CardFinishType =
  | 'MATTE'
  | 'METALLIC_BRUSHED'
  | 'GLOSSY_VIBRANT'
  | 'NEON_PURPLE'
  | 'CENTURION_STEEL'
  | 'CARBON_SLATE'
  | 'LUXURY_GOLD';

export interface RealCardBranding {
  bankDisplayName: string;
  cardModelName: string;
  primaryColorHex: string;
  secondaryColorHex: string;
  accentColorHex: string;
  finishType: CardFinishType;
  network: 'Visa' | 'Mastercard' | 'Amex' | 'Departamental';
  styleNotes: string;
}

export function getFinishDisplayName(type: CardFinishType): string {
  switch (type) {
    case 'MATTE': return 'Mate Satinado';
    case 'METALLIC_BRUSHED': return 'Titanio Cepillado';
    case 'GLOSSY_VIBRANT': return 'Brillo Institucional';
    case 'NEON_PURPLE': return 'Terciopelo Neón';
    case 'CENTURION_STEEL': return 'Acero Centurión';
    case 'CARBON_SLATE': return 'Obsidiana / Carbón';
    case 'LUXURY_GOLD': return 'Dorado Metálico';
  }
}

export function matchRealCardBranding(cardName: string, bank: string, network: string = 'Visa'): RealCardBranding {
  const combined = `${cardName} ${bank}`.toLowerCase();

  // BBVA
  if (combined.includes('bbva')) {
    if (combined.includes('platino') || combined.includes('platinum')) {
      return {
        bankDisplayName: 'BBVA',
        cardModelName: 'Platino',
        primaryColorHex: '#2C3238',
        secondaryColorHex: '#14171A',
        accentColorHex: '#00A9E0',
        finishType: 'METALLIC_BRUSHED',
        network: 'Visa',
        styleNotes: 'Acabado titanio cepillado con vivo BBVA azul cielo'
      };
    }
    if (combined.includes('oro') || combined.includes('gold')) {
      return {
        bankDisplayName: 'BBVA',
        cardModelName: 'Oro',
        primaryColorHex: '#B8860B',
        secondaryColorHex: '#7A5900',
        accentColorHex: '#FFD700',
        finishType: 'LUXURY_GOLD',
        network: 'Visa',
        styleNotes: 'Acabado dorado metálico satinado'
      };
    }
    if (combined.includes('infinite') || combined.includes('black')) {
      return {
        bankDisplayName: 'BBVA',
        cardModelName: 'Infinite',
        primaryColorHex: '#121314',
        secondaryColorHex: '#000000',
        accentColorHex: '#E0E0E0',
        finishType: 'CARBON_SLATE',
        network: 'Visa',
        styleNotes: 'Negro obsidiana mate con bordes reflectivos'
      };
    }
    return {
      bankDisplayName: 'BBVA',
      cardModelName: 'Azul',
      primaryColorHex: '#004481',
      secondaryColorHex: '#042C54',
      accentColorHex: '#00A9E0',
      finishType: 'GLOSSY_VIBRANT',
      network: 'Visa',
      styleNotes: 'Azul marino icónico BBVA con corte diagonal'
    };
  }

  // SANTANDER
  if (combined.includes('santander')) {
    if (combined.includes('likeu') || combined.includes('like u')) {
      if (combined.includes('azul') || combined.includes('blue')) {
        return {
          bankDisplayName: 'Santander',
          cardModelName: 'LikeU Azul',
          primaryColorHex: '#0072CE',
          secondaryColorHex: '#003865',
          accentColorHex: '#EC0000',
          finishType: 'MATTE',
          network: 'Mastercard',
          styleNotes: 'LikeU Personalizable Azul Océano'
        };
      }
      if (combined.includes('rosa') || combined.includes('pink')) {
        return {
          bankDisplayName: 'Santander',
          cardModelName: 'LikeU Rosa',
          primaryColorHex: '#E6007E',
          secondaryColorHex: '#8A004B',
          accentColorHex: '#FFFFFF',
          finishType: 'MATTE',
          network: 'Mastercard',
          styleNotes: 'LikeU Cáncer de Mama Rosa Mate'
        };
      }
      if (combined.includes('verde') || combined.includes('green')) {
        return {
          bankDisplayName: 'Santander',
          cardModelName: 'LikeU Verde',
          primaryColorHex: '#009639',
          secondaryColorHex: '#004D1D',
          accentColorHex: '#FFFFFF',
          finishType: 'MATTE',
          network: 'Mastercard',
          styleNotes: 'LikeU Reforestación Verde Bosque'
        };
      }
      return {
        bankDisplayName: 'Santander',
        cardModelName: 'LikeU Rojo',
        primaryColorHex: '#EC0000',
        secondaryColorHex: '#9E0000',
        accentColorHex: '#FFFFFF',
        finishType: 'GLOSSY_VIBRANT',
        network: 'Mastercard',
        styleNotes: 'Rojo emblemático Santander con relieve de flama'
      };
    }
    return {
      bankDisplayName: 'Santander',
      cardModelName: 'Crédito',
      primaryColorHex: '#EC0000',
      secondaryColorHex: '#8A0000',
      accentColorHex: '#FFFFFF',
      finishType: 'GLOSSY_VIBRANT',
      network: 'Mastercard',
      styleNotes: 'Rojo Santander auténtico con franja estilizada'
    };
  }

  // CITIBANAMEX
  if (combined.includes('citibanamex') || combined.includes('banamex')) {
    if (combined.includes('costco')) {
      return {
        bankDisplayName: 'Banamex',
        cardModelName: 'Costco',
        primaryColorHex: '#005DAA',
        secondaryColorHex: '#003057',
        accentColorHex: '#E01A22',
        finishType: 'MATTE',
        network: 'Visa',
        styleNotes: 'Azul Costco con franja roja de reembolso'
      };
    }
    if (combined.includes('oro') || combined.includes('gold')) {
      return {
        bankDisplayName: 'Banamex',
        cardModelName: 'Oro',
        primaryColorHex: '#C29B38',
        secondaryColorHex: '#7D6017',
        accentColorHex: '#ED1C24',
        finishType: 'LUXURY_GOLD',
        network: 'Mastercard',
        styleNotes: 'Oro brillante con arco rojo Banamex'
      };
    }
    if (combined.includes('platino') || combined.includes('platinum')) {
      return {
        bankDisplayName: 'Banamex',
        cardModelName: 'Platino',
        primaryColorHex: '#374151',
        secondaryColorHex: '#111827',
        accentColorHex: '#ED1C24',
        finishType: 'METALLIC_BRUSHED',
        network: 'Mastercard',
        styleNotes: 'Gris platinado cepillado'
      };
    }
    return {
      bankDisplayName: 'Banamex',
      cardModelName: 'Clásica',
      primaryColorHex: '#002F6C',
      secondaryColorHex: '#001738',
      accentColorHex: '#ED1C24',
      finishType: 'GLOSSY_VIBRANT',
      network: 'Mastercard',
      styleNotes: 'Azul real Banamex con arco rojo institucional'
    };
  }

  // NU
  if (/\bnu\b/i.test(combined) || combined.includes('nubank')) {
    if (combined.includes('ultravioleta')) {
      return {
        bankDisplayName: 'Nu',
        cardModelName: 'Ultravioleta',
        primaryColorHex: '#1C0D2E',
        secondaryColorHex: '#08020F',
        accentColorHex: '#B347EB',
        finishType: 'CARBON_SLATE',
        network: 'Mastercard',
        styleNotes: 'Metal negro con borde láser ultravioleta'
      };
    }
    return {
      bankDisplayName: 'Nu',
      cardModelName: 'Crédito',
      primaryColorHex: '#820AD1',
      secondaryColorHex: '#580391',
      accentColorHex: '#FFFFFF',
      finishType: 'NEON_PURPLE',
      network: 'Mastercard',
      styleNotes: 'Morado Nubank de tacto terciopelo y letras curvas'
    };
  }

  // AMEX
  if (combined.includes('amex') || combined.includes('american express')) {
    if (combined.includes('platinum') || combined.includes('platino')) {
      return {
        bankDisplayName: 'American Express',
        cardModelName: 'The Platinum Card',
        primaryColorHex: '#9E9E9E',
        secondaryColorHex: '#424242',
        accentColorHex: '#2196F3',
        finishType: 'CENTURION_STEEL',
        network: 'Amex',
        styleNotes: 'Acero inoxidable pulido con grabado del Centurión Romano'
      };
    }
    if (combined.includes('gold') || combined.includes('oro')) {
      return {
        bankDisplayName: 'American Express',
        cardModelName: 'Gold Card',
        primaryColorHex: '#D4AF37',
        secondaryColorHex: '#8C6D1F',
        accentColorHex: '#002663',
        finishType: 'LUXURY_GOLD',
        network: 'Amex',
        styleNotes: 'Oro antiguo con patrón guilloché tradicional Amex'
      };
    }
    if (combined.includes('centurion') || combined.includes('black')) {
      return {
        bankDisplayName: 'American Express',
        cardModelName: 'Centurion',
        primaryColorHex: '#171717',
        secondaryColorHex: '#050505',
        accentColorHex: '#CCCCCC',
        finishType: 'CARBON_SLATE',
        network: 'Amex',
        styleNotes: 'Titanio anodizado negro mate de lujo'
      };
    }
    return {
      bankDisplayName: 'American Express',
      cardModelName: 'Green Card',
      primaryColorHex: '#2E6C38',
      secondaryColorHex: '#143B1B',
      accentColorHex: '#002663',
      finishType: 'CENTURION_STEEL',
      network: 'Amex',
      styleNotes: 'Verde monetario clásico Amex con Centurión central'
    };
  }

  // BANORTE
  if (combined.includes('banorte')) {
    if (combined.includes('oro') || combined.includes('gold')) {
      return {
        bankDisplayName: 'Banorte',
        cardModelName: 'Oro',
        primaryColorHex: '#C09B38',
        secondaryColorHex: '#694F10',
        accentColorHex: '#EB0029',
        finishType: 'LUXURY_GOLD',
        network: 'Visa',
        styleNotes: 'Dorado metálico con sello rojo Banorte'
      };
    }
    return {
      bankDisplayName: 'Banorte',
      cardModelName: 'Clásica',
      primaryColorHex: '#EB0029',
      secondaryColorHex: '#8A0014',
      accentColorHex: '#FFFFFF',
      finishType: 'GLOSSY_VIBRANT',
      network: 'Visa',
      styleNotes: 'Rojo pasión Banorte con isotipo institucional'
    };
  }

  // HSBC
  if (combined.includes('hsbc')) {
    if (combined.includes('zero')) {
      return {
        bankDisplayName: 'HSBC',
        cardModelName: 'Zero',
        primaryColorHex: '#2A2A2A',
        secondaryColorHex: '#121212',
        accentColorHex: '#DB0011',
        finishType: 'MATTE',
        network: 'Mastercard',
        styleNotes: 'Negro minimalista mate con hexágono rojo HSBC'
      };
    }
    return {
      bankDisplayName: 'HSBC',
      cardModelName: 'Clásica',
      primaryColorHex: '#FFFFFF',
      secondaryColorHex: '#E0E0E0',
      accentColorHex: '#DB0011',
      finishType: 'MATTE',
      network: 'Visa',
      styleNotes: 'Blanco perla puro con triángulos geométricos HSBC'
    };
  }

  // HEY BANCO
  if (/\bhey\b/i.test(combined)) {
    return {
      bankDisplayName: 'Hey Banco',
      cardModelName: 'Crédito',
      primaryColorHex: '#181818',
      secondaryColorHex: '#080808',
      accentColorHex: '#B4F94C',
      finishType: 'MATTE',
      network: 'Visa',
      styleNotes: 'Negro profundo con acentos verde neón Hey'
    };
  }

  // PLATA CARD
  if (/\bplata\b/i.test(combined)) {
    return {
      bankDisplayName: 'Plata',
      cardModelName: 'Card',
      primaryColorHex: '#222222',
      secondaryColorHex: '#111111',
      accentColorHex: '#00D1FF',
      finishType: 'MATTE',
      network: 'Mastercard',
      styleNotes: 'Gris plomo mate con línea cian luminosa'
    };
  }

  // LIVERPOOL
  if (combined.includes('liverpool')) {
    return {
      bankDisplayName: 'Liverpool',
      cardModelName: combined.includes('visa') ? 'Visa' : 'Departamental',
      primaryColorHex: '#D81B60',
      secondaryColorHex: '#880E4F',
      accentColorHex: '#FFFFFF',
      finishType: 'GLOSSY_VIBRANT',
      network: combined.includes('visa') ? 'Visa' : 'Departamental',
      styleNotes: 'Rosa magenta Liverpool con tipografía elegante'
    };
  }

  // PALACIO DE HIERRO
  if (combined.includes('palacio')) {
    return {
      bankDisplayName: 'Palacio de Hierro',
      cardModelName: 'Palacio',
      primaryColorHex: '#E5B80B',
      secondaryColorHex: '#8F6D00',
      accentColorHex: '#111111',
      finishType: 'LUXURY_GOLD',
      network: 'Departamental',
      styleNotes: 'Amarillo oro icónico Totalmente Palacio'
    };
  }

  // SEARS
  if (combined.includes('sears')) {
    return {
      bankDisplayName: 'Sears',
      cardModelName: 'Sears',
      primaryColorHex: '#003087',
      secondaryColorHex: '#001A4B',
      accentColorHex: '#FFFFFF',
      finishType: 'GLOSSY_VIBRANT',
      network: 'Departamental',
      styleNotes: 'Azul marino Sears clásico'
    };
  }

  // DEFAULT
  return {
    bankDisplayName: bank || 'Banco',
    cardModelName: cardName || 'Crédito',
    primaryColorHex: '#1E293B',
    secondaryColorHex: '#0F172A',
    accentColorHex: '#38BDF8',
    finishType: 'METALLIC_BRUSHED',
    network: (network as 'Visa' | 'Mastercard' | 'Amex' | 'Departamental') || 'Visa',
    styleNotes: 'Acabado moderno con chip metálico'
  };
}
