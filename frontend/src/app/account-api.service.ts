import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Account, AccountPayload } from './account.model';

@Injectable({
  providedIn: 'root',
})
export class AccountApiService {
  private readonly baseUrl = 'http://localhost:8080/api/accounts';

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Account[]> {
    return this.http.get<Account[]>(this.baseUrl);
  }

  create(account: AccountPayload): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, account);
  }

  update(id: number, account: AccountPayload): Observable<Account> {
    return this.http.put<Account>(`${this.baseUrl}/${id}`, account);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
