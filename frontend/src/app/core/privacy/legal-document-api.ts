import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

import { LegalDocumentDto, LegalDocumentType } from '../../domain/legal-document.model';
import { API_URL } from '../http/api-url';

@Service()
export class LegalDocumentApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_URL);

  getByType(type: LegalDocumentType): Observable<LegalDocumentDto> {
    return this.http.get<LegalDocumentDto>(`${this.apiUrl}/documents/${type}`);
  }
}
