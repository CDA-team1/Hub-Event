export type LegalDocumentType = 'CGU' | 'RGPD';

export interface LegalDocumentDto {
  id: number;
  type: LegalDocumentType;
  content: string;
  updatedAt: string;
}
