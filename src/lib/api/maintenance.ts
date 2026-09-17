import { apiRequest } from "./client";

export type Maintenance = {
  id: number;
  orderNumber: string;
  aircraft: string;
  type: string;
  severity: "Routine" | "Minor" | "Major" | "Critical";
  due: string;
  progress: number;
  engineer: string;
};

export async function getMaintenance(): Promise<Maintenance[]> {
  return apiRequest<Maintenance[]>("/maintenance");
}
