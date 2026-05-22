type RelacionamentoComContexto = {
  setor?: string | null;
  versao?: string | null;
};

export function totalSetoresRelacionados(itens: RelacionamentoComContexto[]): number {
  return new Set(
    itens
      .map((item) => item.setor)
      .filter((setor): setor is string => Boolean(setor))
  ).size;
}

export function ultimaVersaoRelacionada(itens: RelacionamentoComContexto[]): string {
  return itens.find((item) => item.versao)?.versao ?? '-';
}

export function criarMetricasRelacionamento(
  quantidadeLabel: string,
  quantidade: number,
  setoresLabel: string,
  setores: number,
  ultimaVersao: string
) {
  return [
    {
      label: quantidadeLabel,
      value: quantidade
    },
    {
      label: setoresLabel,
      value: setores
    },
    {
      label: 'Última versão vista',
      value: ultimaVersao,
      tone: 'warm' as const
    }
  ];
}

export function resumoQuantidadeRelacionamentos(
  entidadeSingular: string,
  entidadePlural: string,
  total: number
): string {
  if (!total) {
    return `Nenhum ${entidadeSingular} vinculado por enquanto.`;
  }

  if (total === 1) {
    return `Este serviço aparece em 1 ${entidadeSingular} da base operacional.`;
  }

  return `Este serviço aparece em ${total} ${entidadePlural} da base operacional.`;
}
