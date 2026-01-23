import { createReducer, on } from "@ngrx/store";
import { Student } from "src/app/models/Student.model";
import * as STUDENTACTIONS from "./student.actions";



export interface StudentState{
    students: Student[];
    loading: boolean;   
    error: any;
}

export const initialStudentState: StudentState = {
    students: [],
    loading: false,
    error: null
};

export const STUDENT_FEATURE_KEY = 'students';

export const studentReducer = createReducer(
    initialStudentState,

    on(STUDENTACTIONS.loadStudents, (state) => ({
        ...state,
        loading: true,
        error: null
    })),

    on(STUDENTACTIONS.loadStudentsSuccess, (state, { students }) => ({
        ...state,
        students: students,
        loading: false,
        error: null
    })),

    on(STUDENTACTIONS.loadStudentsFailure, (state, { error }) => ({
        ...state,
        loading: false,
        error: error
    })),

    on(STUDENTACTIONS.addStudentSuccess, (state, { student }) => ({
        ...state,
        students: [...state.students, student],
        error: null
    })),

    on(STUDENTACTIONS.addStudentFailure, (state, { error }) => ({
        ...state,
        error: error
    })),

    on(STUDENTACTIONS.updateStudentSuccess, (state, { student }) => ({
        ...state,
        students: state.students.map(s => s.id === student.id ? student : s),
        error: null
    })),

    on(STUDENTACTIONS.updateStudentFailure, (state, { error }) => ({
        ...state,
        error: error
    })),

    on(STUDENTACTIONS.deleteStudentSuccess, (state, { studentId }) => ({
        ...state,
        students: state.students.filter(s => s.id !== studentId),
        error: null
    })),

    on(STUDENTACTIONS.deleteStudentFailure, (state, { error }) => ({
        ...state,
        error: error
    }))
)