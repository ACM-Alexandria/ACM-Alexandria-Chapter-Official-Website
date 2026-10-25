// Wording of the registration dialogs for each kind of thing a visitor can sign up for.
const copy = {
  event: { eyebrow: "Event registration", verb: "register for", done: "You're registered" },
  club: { eyebrow: "Club application", verb: "join", done: "Application sent" },
  program: { eyebrow: "Program registration", verb: "register for", done: "You're registered" },
  committee: { eyebrow: "Committee application", verb: "apply to", done: "Application sent" },
  "exclusive-form": { eyebrow: "Application", verb: "apply to", done: "Application sent" },
};

export const registrationCopy = (type) => copy[type] || copy.event;
