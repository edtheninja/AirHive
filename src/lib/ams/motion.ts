import type { Transition, Variants } from "motion/react";

export const spring: Transition = { type: "spring", stiffness: 260, damping: 28, mass: 0.9 };
export const softSpring: Transition = { type: "spring", stiffness: 180, damping: 24 };

export const pageVariants: Variants = {
  initial: { opacity: 0, y: 12 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.42, ease: [0.32, 0.72, 0, 1] } },
  exit: { opacity: 0, y: -8, transition: { duration: 0.2 } },
};

export const listVariants: Variants = {
  animate: { transition: { staggerChildren: 0.045 } },
};

export const itemVariants: Variants = {
  initial: { opacity: 0, y: 10 },
  animate: { opacity: 1, y: 0, transition: spring },
};

export const interactive = {
  whileHover: { scale: 1.02 },
  whileTap: { scale: 0.98 },
  transition: spring,
};
