import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Student } from '../models/Student.model';

@Injectable({
  providedIn: 'root'
})
export class StudentsService {

  constructor(private http: HttpClient) { }

  //URL FOR THE BACKEND API
  private url = "http://localhost:4200"

  //Retrieve all students
  getStudents(): Observable<Student[]> {
    return this.http.get<Student[]>(`${this.url}/students`);
  }

}
