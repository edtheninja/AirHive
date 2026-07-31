# Skyward Ops

Create a modern, premium Airline Management System (AMS) web application.

This is NOT a booking website. It is a professional airline operations platform used by airline administrators, airport staff, operations managers, and supervisors.

Design Philosophy

The interface should feel inspired by modern macOS design language without copying Apple. Focus on elegance, clarity, simplicity, spatial depth, and smooth motion.

Use:

• Frosted glass surfaces

• Soft layered shadows

• Rounded corners (18–24px)

• Clean typography

• Large breathing spaces

• Minimal visual clutter

• Intelligent information hierarchy

• Premium enterprise appearance

Avoid:

• Material Design look

• Bootstrap styling

• Heavy gradients

• Loud colors

• Oversized buttons

• Boxy layouts

• Generic admin dashboards

-----------------------------------

Color Palette

Background:

#F6F7FB

Primary:

#1E3A8A

Accent:

#3B82F6

Success:

#22C55E

Warning:

#F59E0B

Danger:

#EF4444

Card:

rgba(255,255,255,0.72)

Glass Blur:

blur(28px)

Border:

rgba(255,255,255,0.25)

Text Primary:

#111827

Text Secondary:

#6B7280

-----------------------------------

Typography

Use Inter.

Headings:

SemiBold

Body:

Regular

Numbers:

Monospace variant

Large KPI values should feel like macOS widgets.

-----------------------------------

Layout

Permanent left sidebar.

Top command bar.

Large dashboard workspace.

Everything aligned on an 8px grid.

Sidebar contains icons with labels.

Dashboard should never feel crowded.

-----------------------------------

Sidebar Sections

Dashboard

Flights

Aircraft

Routes

Passengers

Bookings

Crew

Airports

Maintenance

Operations

Analytics

Notifications

Settings

User Profile

Icons should be modern outline icons with active-state animations.

-----------------------------------

Dashboard

Create a premium airline operations dashboard.

Top Row

• Total Flights

• Active Flights

• Delayed Flights

• Revenue Today

• Passenger Count

• Fleet Availability

Each metric inside floating glass cards.

Cards should slightly elevate when hovered.

-----------------------------------

Live Flight Operations

Large table containing

Flight Number

Aircraft

Origin

Destination

Gate

Boarding Status

Departure Time

Delay

Crew

Current Status

Statuses use animated pills

Scheduled

Boarding

Taxiing

Departed

In Air

Landing

Landed

Delayed

Cancelled

-----------------------------------

Live World Map

Reserve a large section for future real-time aircraft tracking.

Use animated placeholder flight paths.

Soft glowing aircraft markers.

-----------------------------------

Quick Actions

Add Flight

Assign Aircraft

Schedule Crew

Generate Report

Emergency Alert

Publish Delay

-----------------------------------

Analytics

Modern charts

Revenue

Occupancy

Flight Completion

Fuel Usage

Delay Analysis

Charts should use thin elegant strokes.

-----------------------------------

Notifications Panel

Glass floating panel.

Shows live operational events.

Examples:

Flight AI302 delayed by 15 minutes

Gate changed to A12

Aircraft VT-AXN ready

Crew assigned

Passenger checked in

Updates should animate into view.

-----------------------------------

Aircraft Management

Minimal card layout.

Each aircraft card contains

Aircraft image

Registration

Model

Capacity

Health

Fuel

Maintenance

Location

Status

Cards gently scale on hover.

-----------------------------------

Booking Management

Professional enterprise table.

Search

Filters

Pagination

Quick actions

Export

Booking details open in floating modal.

-----------------------------------

Crew Management

Card + table hybrid.

Crew availability.

Upcoming schedules.

Rest hours.

Medical clearance.

-----------------------------------

Animations

Animation quality should feel comparable to modern operating systems.

Do NOT use exaggerated bounce effects.

Everything should feel physically responsive.

Interaction Rules

Hover:

Scale 1.02

Press:

Scale 0.98

Card Elevation:

Shadow increases smoothly

Page transitions:

Fade + slight upward movement

Sidebar expansion:

Spring animation

Dialogs:

Fade + scale from 96% to 100%

Tables:

Rows animate independently

Notifications:

Slide in from top-right

Dropdowns:

Fade and blur

Loading:

Skeleton shimmer

Scrolling:

Very smooth

Motion should use spring physics with realistic damping.

-----------------------------------

Real-Time Behaviour

The interface must look alive.

Every few seconds

Flight statuses update

Passenger count changes

Notifications arrive

Boarding progress increases

Aircraft availability changes

Charts update smoothly

No page refreshes.

Use animated transitions for changing values.

Numbers should count smoothly.

Progress bars should interpolate.

-----------------------------------

Dark Mode

Create a beautiful dark theme.

Background:

#0F172A

Glass:

rgba(30,41,59,.65)

Cards should glow subtly.

Charts adapt automatically.

-----------------------------------

Responsive

Desktop first.

Then tablet.

Then mobile.

On mobile

Sidebar becomes floating navigation.

Cards stack intelligently.

Tables become responsive.

-----------------------------------

Tech Requirements

React

TypeScript

TailwindCSS

Framer Motion

shadcn/ui

Lucide React Icons

React Router

Component-driven architecture.

Use reusable components.

Avoid hardcoded values.

The UI should feel like enterprise software used by airlines such as Emirates, Lufthansa, Singapore Airlines, or Delta operations centers.

The result should be a production-quality, premium UI suitable for a scalable Airline Management System that can later connect seamlessly to a Spring Boot backend.

This project was built with [Lovable](https://lovable.dev).

**Live app**: https://skyward-zenith-ops.lovable.app

## Build with Lovable

Continue developing this project in the [Lovable editor](https://lovable.dev/projects/df8b1868-feef-4438-9a0a-d5022b91e158).

- **Ship faster**: describe what you want to build and Lovable handles the code.
- **Stay in sync**: every change made in Lovable is committed straight to this repository.
- **Full ownership**: this code is yours. Push to `main` on GitHub and your changes sync back into Lovable, ready for your next prompt.

## Development

Prefer working locally? You need Node.js and npm — [install with nvm](https://github.com/nvm-sh/nvm#installing-and-updating).

```sh
git clone <this-repository-url>
cd <repository-name>
npm i
npm run dev
```
