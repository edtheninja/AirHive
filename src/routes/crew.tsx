import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { motion } from "motion/react";
import { pageVariants, itemVariants, listVariants } from "@/lib/ams/motion";
import { getCrew, type ApiCrewMember } from "@/lib/api/crew";
import { GlassCard, Meter, PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/crew")({
  head: () => ({
    meta: [
      { title: "Crew Management — AirHive AMS" },
      {
        name: "description",
        content: "Crew availability, rest hours, rosters and medical clearance tracking.",
      },
      { property: "og:title", content: "Crew Management — AirHive AMS" },
      {
        property: "og:description",
        content: "Crew availability, rest hours and medical clearance tracking.",
      },
    ],
  }),
  component: CrewPage,
});

type CrewMember = ApiCrewMember;

const availTone: Record<CrewMember["availability"], string> = {
  Available: "bg-success/14 text-success",
  "On Duty": "bg-info/12 text-info",
  Resting: "bg-warning/16 text-warning",
  Leave: "bg-foreground/6 text-muted-foreground",
};

function CrewPage() {
  const [crew, setCrew] = useState<CrewMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    async function loadCrew() {
      try {
        setLoading(true);
        setError("");

        const crewData = await getCrew();

        if (active) {
          setCrew(
            crewData.map((member) => ({
              ...member,
              role: member.role === "FirstOfficer" ? "First Officer" : member.role,
            })),
          );
        }
      } catch {
        if (active) {
          setError("Unable to load crew data.");
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    void loadCrew();

    return () => {
      active = false;
    };
  }, []);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader title="Crew" description="Crew roster, readiness and duty information." />

      <motion.div
        variants={listVariants}
        initial="initial"
        animate="animate"
        className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4"
      >
        {crew.slice(0, 4).map((c) => (
          <motion.div key={c.id} variants={itemVariants}>
            <GlassCard className="p-5">
              <div className="flex items-center gap-3">
                <span className="num flex h-11 w-11 items-center justify-center rounded-2xl bg-primary text-sm font-semibold text-primary-foreground">
                  {c.initials}
                </span>
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium">{c.name}</p>
                  <p className="text-xs text-muted-foreground">
                    {c.role} · {c.base}
                  </p>
                </div>
              </div>
              <span
                className={`mt-4 inline-block rounded-full px-2.5 py-1 text-xs ${availTone[c.availability]}`}
              >
                {c.availability}
              </span>
              <div className="mt-4">
                <div className="mb-1 flex justify-between text-xs text-muted-foreground">
                  <span>Rest hours</span>
                  <span className="num">{c.restHours}h</span>
                </div>
                <Meter
                  value={(c.restHours / 24) * 100}
                  tone={c.restHours >= 10 ? "success" : "warning"}
                />
              </div>
            </GlassCard>
          </motion.div>
        ))}
      </motion.div>

      <SectionCard title="Crew roster" subtitle="Live crew roster, readiness and duty information">
        {loading && <p className="px-3 py-4 text-sm text-muted-foreground">Loading crew...</p>}

        {!loading && error && <p className="px-3 py-4 text-sm text-destructive">{error}</p>}

        {!loading && !error && crew.length === 0 && (
          <p className="px-3 py-4 text-sm text-muted-foreground">No crew members found.</p>
        )}

        {!loading && !error && crew.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[760px] border-separate border-spacing-y-1 text-sm">
              <thead>
                <tr className="text-left text-xs text-muted-foreground">
                  {["Crew", "Role", "Base", "Availability", "Rest", "Next flight", "Medical"].map(
                    (h) => (
                      <th key={h} className="px-3 pb-2 font-medium">
                        {h}
                      </th>
                    ),
                  )}
                </tr>
              </thead>
              <tbody>
                {crew.map((c, i) => (
                  <motion.tr
                    key={c.id}
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: i * 0.03 }}
                    className="hover:bg-foreground/4"
                  >
                    <td className="rounded-l-2xl px-3 py-3 font-medium">{c.name}</td>
                    <td className="px-3 py-3 text-muted-foreground">{c.role}</td>
                    <td className="num px-3 py-3">{c.base}</td>
                    <td className="px-3 py-3">
                      <span
                        className={`rounded-full px-2.5 py-1 text-xs ${availTone[c.availability]}`}
                      >
                        {c.availability}
                      </span>
                    </td>
                    <td className="num px-3 py-3">{c.restHours}h</td>
                    <td className="num px-3 py-3">{c.nextFlight}</td>
                    <td className="rounded-r-2xl px-3 py-3 text-muted-foreground">{c.medical}</td>
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </SectionCard>
    </motion.div>
  );
}
