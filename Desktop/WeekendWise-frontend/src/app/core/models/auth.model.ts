export interface AuthResponse {
  token: string;
  user: UserProfile;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  preferredCurrency: string;
  homeAirport: string;
}

export interface UserProfile {
  id: string;
  fullName: string;
  email: string;
  preferredCurrency: string;
  homeAirport: string;
}
