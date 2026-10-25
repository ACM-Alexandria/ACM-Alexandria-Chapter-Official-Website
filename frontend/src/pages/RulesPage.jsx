import { Link } from "react-router-dom";
import { FiAlertTriangle, FiArrowRight, FiAward, FiCheckSquare, FiClock, FiLock, FiMessageSquare, FiShield, FiUsers } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import PageHero from "../components/Layout/PageHero";
import Reveal from "../components/ui/Reveal";
import { useAuth } from "../contexts/useAuth";
import { card, muted, primaryButton } from "../components/ui/uiStyles";

const MEMBER_ROLES = ["ACM_MEMBER", "ACM_CLUB_BOARD", "ACM_COMMITTEE_BOARD", "ACM_HIGH_BOARD", "SUPER_ADMIN"];

const conduct = [
  { icon: FiClock, title: "Commitment & Reliability", text: "We value everyone's time. Members are expected to be punctual for meetings, honor their task deadlines, and actively participate in chapter activities." },
  { icon: FiMessageSquare, title: "Proactive Communication", text: "Transparency is key to teamwork. If a member cannot meet a deadline or attend a meeting, we expect them to communicate with their team leads in advance." },
  { icon: FiUsers, title: "Shared Responsibility", text: "We thrive on collaboration. In team projects, roles must be clearly defined, and credit is shared fairly among all contributors." },
  { icon: FiShield, title: "Professionalism & Respect", text: "We foster a supportive and inclusive environment. Every member is expected to treat their peers with kindness, respect, and professionalism at all times." },
];

const Strong = ({ children }) => <strong className="font-semibold text-slate-900 dark:text-white">{children}</strong>;

const policies = [
  {
    icon: FiCheckSquare,
    title: "Task Management",
    rules: [
      { title: "Deadlines", text: <>Tasks must be submitted by the agreed-upon deadline. Submitting a task late without a pre-communicated excuse will result in a <Strong>Formal Warning</Strong>.</> },
      { title: "Task Execution", text: <>Tasks must be completed accurately and according to the provided instructions. Incorrect execution due to negligence will result in a <Strong>Verbal Warning</Strong> (a formal heads-up).</> },
      { title: "Co-Management Reporting", text: <>For any project managed by two or more individuals, the exact distribution of responsibilities must be reported to the HR committee. Failure to submit this distribution will result in a <Strong>Verbal Warning</Strong> for the managers involved.</> },
    ],
  },
  {
    icon: FiClock,
    title: "Attendance & Communication",
    rules: [
      { title: "Punctuality & Attendance", text: <>Members are required to attend scheduled meetings and arrive on time. Unexcused absences or failure to commit to meeting times will result in a <Strong>Formal Warning</Strong>.</> },
      { title: "Ghosting", text: <>Ignoring communications, abandoning accepted tasks, or taking a silent absence without a proper reason disrupts community operations. Any form of ghosting will result in a <Strong>Formal Warning</Strong>.</> },
    ],
  },
  {
    icon: FiAlertTriangle,
    title: "Warning & Filtration System",
    rules: [
      { title: "Verbal Warning", tone: "bg-amber-400", text: "Issued as a formal notice for minor infractions (e.g., incorrect task execution or reporting failures). Accumulating two (2) Verbal Warnings equates to one (1) Formal Warning." },
      { title: "Formal Warning", tone: "bg-orange-500", text: "Issued for direct policy breaches regarding deadlines, attendance, and communication." },
      { title: "Filtration", tone: "bg-red-600", text: "Accumulating three (3) Formal Warnings will result in immediate filtration from the chapter." },
      { title: "Warning Deduction", tone: "bg-emerald-500", text: "Demonstrating outstanding performance or exceptional effort can reduce a member’s record by one (1) Formal Warning." },
      { title: "Behavioral Violations", tone: "bg-slate-500", text: "Any unethical or inappropriate behavioral actions bypass the standard warning system and are subject to immediate escalation." },
    ],
  },
  {
    icon: FiAward,
    title: "Performance Tracking & Recognition",
    rules: [
      { title: "Point System", text: "Member performance is tracked through a point system. Points are collected by completing tasks on time and maintaining consistent engagement." },
      { title: "Best Member", text: <>Increased participation and active engagement yield higher point totals. The members with the highest accumulated points will earn the <Strong>&ldquo;Best Member&rdquo;</Strong> title.</> },
    ],
  },
];

const IconBox = ({ icon }) => {
  const Icon = icon;
  return <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-[#E2F0FA] text-[#205E85] dark:bg-[#16364C] dark:text-[#7DBBE0]"><Icon className="h-5 w-5" aria-hidden="true" /></span>;
};

// The chapter's code of conduct for everyone, plus the internal policies that members see once signed in.
const RulesPage = () => {
  const { isAuthenticated, user } = useAuth();
  const isMember = isAuthenticated && MEMBER_ROLES.includes(user?.role);

  return (
    <SiteLayout>
      <PageHero eyebrow="Rules" title="How we work together" text="Guiding principles, collaborative values, and standards of excellence for the ACM Alexandria Student Chapter community." />
      <div className="mx-auto max-w-[1280px] space-y-20 px-5 py-16 sm:px-8 sm:py-20">
        <section id="code-of-conduct" aria-labelledby="conduct-heading">
          <Reveal>
            <h2 id="conduct-heading" className="text-3xl font-semibold tracking-tight text-slate-900 sm:text-4xl dark:text-white">Code of conduct</h2>
            <p className={`mt-2 text-lg ${muted}`}>What every member of the community can expect from each other.</p>
          </Reveal>
          <div className="mt-8 grid gap-5 md:grid-cols-2">
            {conduct.map((item, i) => (
              <Reveal key={item.title} delay={(i % 2) * 80}>
                <article className={`flex h-full gap-5 rounded-[2rem] p-6 sm:p-8 ${card}`}>
                  <IconBox icon={item.icon} />
                  <div>
                    <h3 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-white">{item.title}</h3>
                    <p className={`mt-2 leading-relaxed ${muted}`}>{item.text}</p>
                  </div>
                </article>
              </Reveal>
            ))}
          </div>
        </section>

        <section id="internal-policies" aria-labelledby="policies-heading">
          <Reveal>
            <h2 id="policies-heading" className="text-3xl font-semibold tracking-tight text-slate-900 sm:text-4xl dark:text-white">Internal member policies</h2>
            <p className={`mt-2 text-lg ${muted}`}>How tasks, attendance, warnings, and recognition work inside the chapter.</p>
          </Reveal>
          {isMember ? (
            <div className="mt-8 space-y-5">
              {policies.map((policy, i) => (
                <Reveal key={policy.title}>
                  <article className={`rounded-[2rem] p-6 sm:p-8 ${card}`}>
                    <div className="flex items-center gap-4">
                      <IconBox icon={policy.icon} />
                      <h3 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-white"><span className="text-[#2A7AB0] dark:text-[#7DBBE0]">{i + 1}.</span> {policy.title}</h3>
                    </div>
                    <dl className="mt-6 grid gap-4 md:grid-cols-2">
                      {policy.rules.map((rule) => (
                        <div key={rule.title} className="rounded-2xl bg-[#F3F7FB] p-5 dark:bg-white/5">
                          <dt className="flex items-center gap-2 font-semibold text-slate-900 dark:text-white">{rule.tone && <span className={`h-2.5 w-2.5 rounded-full ${rule.tone}`} aria-hidden="true" />}{rule.title}</dt>
                          <dd className={`mt-2 text-sm leading-relaxed ${muted}`}>{rule.text}</dd>
                        </div>
                      ))}
                    </dl>
                  </article>
                </Reveal>
              ))}
            </div>
          ) : (
            <Reveal className={`mt-8 flex flex-col items-start gap-4 rounded-[2rem] p-6 sm:flex-row sm:items-center sm:p-8 ${card}`}>
              <IconBox icon={FiLock} />
              <p className={`flex-1 ${muted}`}>These policies are shared with chapter members. Sign in with your member account to read them.</p>
              {!isAuthenticated && <Link to="/login" className={primaryButton}>Sign in</Link>}
            </Reveal>
          )}
        </section>

        <Reveal className="flex flex-col items-start justify-between gap-6 rounded-[2rem] bg-[#0F3A5A] p-8 text-white sm:flex-row sm:items-center sm:p-10 dark:bg-[#12304A]">
          <div>
            <h2 className="text-xl font-semibold tracking-tight">Questions about the rules?</h2>
            <p className="mt-1 text-[#CFE6F5]">Reach out to your committee leaders or the ACM Alexandria HR Board for clarifications.</p>
          </div>
          <Link to="/" className="inline-flex h-11 shrink-0 items-center gap-2 rounded-xl bg-white px-5 text-sm font-semibold text-[#205E85] transition-colors hover:bg-[#E2F0FA]">Back to home <FiArrowRight className="h-4 w-4" aria-hidden="true" /></Link>
        </Reveal>
      </div>
    </SiteLayout>
  );
};

export default RulesPage;
