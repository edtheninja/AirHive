import { useEffect, useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { pageVariants } from "@/lib/ams/motion";
import { getMaintenance, type Maintenance } from "@/lib/api/maintenance";
import { Meter, PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/maintenance")({
  head: () => ({
    meta: [
      { title: "Maintenance — AirHive AMS" },
      {
        name: "description",
        content: "Work orders, severity, engineers and completion progress for the fleet.",
      },
      { property: "og:title", content: "Maintenance — AirHive AMS" },
      { property: "og:description", content: "Work orders and completion progress for the fleet." },
    ],
  }),
  component: MaintenancePage,
});

const severityTone: Record<string, string> = {
  Routine: "bg-info/12 text-info",
  Minor: "bg-foreground/6 text-muted-foreground",
  Major: "bg-warning/16 text-warning",
  Critical: "bg-destructive/14 text-destructive",
};

function MaintenancePage() {
  const [maintenance, setMaintenance] = useState<Maintenance[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    async function loadMaintenance() {
      try {
        const data = await getMaintenance();

        if (mounted) {
          setMaintenance(data);
        }
      } catch (err) {
        if (mounted) {
          setError(err instanceof Error ? err.message : "Unable to load maintenance.");
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    void loadMaintenance();

    return () => {
      mounted = false;
    };
  }, []);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader
        title="Maintenance"
        description="Maintenance work orders, severity and completion progress."
      />

      <SectionCard
        title="Work orders"
        subtitle={
          loading
            ? "Loading maintenance records..."
            : error
              ? "Unable to load maintenance records"
              : `${maintenance.length} work orders`
        }
      >
        <div className="overflow-x-auto">
          <table className="w-full min-w-[760px] border-separate border-spacing-y-1 text-sm">
            <thead>
              <tr className="text-left text-xs text-muted-foreground">
                {["Order", "Aircraft", "Type", "Severity", "Due", "Progress", "Engineer"].map(
                  (h) => (
                    <th key={h} className="px-3 pb-2 font-medium">
                      {h}
                    </th>
                  ),
                )}
              </tr>
            </thead>

            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={7} className="px-3 py-12 text-center text-muted-foreground">
                    Loading maintenance records...
                  </td>
                </tr>
              ) : error ? (
                <tr>
                  <td colSpan={7} className="px-3 py-12 text-center text-destructive">
                    {error}
                  </td>
                </tr>
              ) : maintenance.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-3 py-12 text-center text-muted-foreground">
                    No maintenance records found.
                  </td>
                </tr>
              ) : (
                maintenance.map((m, i) => (
                  <motion.tr
                    key={m.id}
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: i * 0.04 }}
                    className="hover:bg-foreground/4"
                  >
                    <td className="num rounded-l-2xl px-3 py-3 font-medium">
                      {m.orderNumber}
                    </td>
                    <td className="num px-3 py-3">{m.aircraft}</td>
                    <td className="px-3 py-3">{m.type}</td>
                    <td className="px-3 py-3">
                      <span
                        className={`rounded-full px-2.5 py-1 text-xs ${severityTone[m.severity]}`}
                      >
                        {m.severity}
                      </span>
                    </td>
                    <td className="px-3 py-3 text-muted-foreground">{m.due}</td>
                    <td className="w-44 px-3 py-3">
                      <Meter value={m.progress} tone={m.progress > 60 ? "success" : "accent"} />
                      <span className="num mt-1 block text-[11px] text-muted-foreground">
                        {m.progress}%
                      </span>
                    </td>
                    <td className="rounded-r-2xl px-3 py-3 text-muted-foreground">
                      {m.engineer}
                    </td>
                  </motion.tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </SectionCard>
    </motion.div>
  );
}
