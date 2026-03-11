import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PaymentRequest {
  nameFirst: string;
  nameLast: string;
  emailAddress: string;
  cellNumber?: string;
  amount: number;
  itemName: string;
  itemDescription?: string;
  mPaymentId?: string;
}

export interface PaymentFormData {
  actionUrl: string;
  formFields: { [key: string]: string };
}

@Injectable({
  providedIn: 'root'
})
export class PayfastService {

  private apiUrl = `${environment.apiUrl}/payments`;

  constructor(private http: HttpClient) {}

  createPayment(request: PaymentRequest): Observable<PaymentFormData> {
    return this.http.post<PaymentFormData>(`${this.apiUrl}/create`, request);
  }

  submitToPayfast(formData: PaymentFormData): void {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = formData.actionUrl;

    Object.entries(formData.formFields).forEach(([key, value]) => {
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = key;
      input.value = value;
      form.appendChild(input);
    });

    document.body.appendChild(form);
    form.submit();
  }
}
