import { useContext } from "react";
import { LiveOpsContext, type LiveOpsValue } from "./context";

export function useLiveOps(): LiveOpsValue {
  const ctx = useContext(LiveOpsContext);
  if (!ctx) throw new Error("useLiveOps must be used inside LiveOpsProvider");
  return ctx;
}
