export type TripStyle = 'RELAXING' | 'ADVENTURE' | 'NIGHTLIFE' | 'CULTURE' | 'FAMILY';
export type SortOption = 'BEST_MATCH' | 'CHEAPEST' | 'BEST_WEATHER';

export interface TripSearchRequest {
  origin: string;
  startDate: string;
  endDate: string;
  budget: number;
  travelers: number;
  tripStyle: TripStyle;
  region: string;
}

export interface Attraction {
  name: string;
  category: string;
  rating: number;
  address: string;
}

export interface DailyForecast {
  day: string;
  condition: string;
  minTemp: number;
  maxTemp: number;
  rainChance: number;
}

export interface FlightSummary {
  airline: string;
  departureTime: string;
  returnTime: string;
  duration: string;
  price: number;
}

export interface HotelSummary {
  name: string;
  area: string;
  rating: number;
  pricePerNight: number;
  totalPrice: number;
}

export interface DestinationRecommendation {
  id: string;
  destinationCity: string;
  country: string;
  region: string;
  score: number;
  flightPrice: number;
  hotelPrice: number;
  totalEstimatedCost: number;
  weatherSummary: string;
  weatherScore: number;
  imageUrl: string;
  topAttractions: Attraction[];
  flight: FlightSummary;
  hotel: HotelSummary;
}

export interface DestinationDetail extends DestinationRecommendation {
  overview: string;
  dailyForecast: DailyForecast[];
  itinerary: string[];
  tags: string[];
}

export interface FavoriteTripPayload {
  destinationId: string;
}
