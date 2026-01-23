import { createAction, props } from "@ngrx/store";
import { Student } from "../../models/Student.model";

//LOAD STUDENTS
export const loadStudents = createAction(
    "[Student] Load Students",
)

//LOAD STUDENTS SUCCESS
export const loadStudentsSuccess = createAction(
    "[Student] Load Students Success",
    props<{ students: Student[] }>()
)

//LOAD STUDENTS FAILURE
export const loadStudentsFailure = createAction(
    "[Student] Load Students Failure",
    props<{ error: any }>()
)

//ADD STUDENT
export const addStudent = createAction(
    "[Student] Add Student",
    props<{ student: Student }>()
)

//ADD STUDENT SUCCESS
export const addStudentSuccess = createAction(
    "[Student] Add Student Success",
    props<{ student: Student }>()
)

//ADD STUDENT FAILURE
export const addStudentFailure = createAction(
    "[Student] Add Student Failure",
    props<{ error: any }>()
)

//UPDATE STUDENT
export const updateStudent = createAction(
    "[Student] Update Student",
    props<{ student: Student }>()
)

//UPDATE STUDENT SUCCESS
export const updateStudentSuccess = createAction(
    "[Student] Update Student Success",
    props<{ student: Student }>()
)

//UPDATE STUDENT FAILURE
export const updateStudentFailure = createAction(
    "[Student] Update Student Failure",
    props<{ error: any }>()
)

//DELETE STUDENT
export const deleteStudent = createAction(
    "[Student] Delete Student",
    props<{ studentId: number }>()
)

//DELETE STUDENT SUCCESS
export const deleteStudentSuccess = createAction(
    "[Student] Delete Student Success",
    props<{ studentId: number }>()
)

//DELETE STUDENT FAILURE
export const deleteStudentFailure = createAction(
    "[Student] Delete Student Failure",
    props<{ error: any }>()
)