import { Link } from "react-router-dom";
import Navbar from "../components/HomePage/Navbar";
import Footer from "../components/HomePage/Footer";
import { useAuth } from "../contexts/AuthContext";
import {
  FiArrowLeft,
  FiClock,
  FiMessageSquare,
  FiUsers,
  FiShield,
  FiAlertTriangle,
  FiAward,
  FiCheckSquare,
  FiTrendingUp,
  FiChevronRight,
} from "react-icons/fi";

const MEMBER_ROLES = [
  "ACM_MEMBER",
  "ACM_CLUB_BOARD",
  "ACM_COMMITTEE_BOARD",
  "ACM_HIGH_BOARD",
  "SUPER_ADMIN",
];

const RulesPage = () => {
  const { isAuthenticated, user } = useAuth();

  const isMemberOrAbove =
    isAuthenticated && user?.role && MEMBER_ROLES.includes(user.role);

  return (
    <div className="min-h-screen flex flex-col bg-white dark:bg-slate-950 text-slate-800 dark:text-slate-100 transition-colors duration-300">
      <Navbar activeSection="" />

      <main className="flex-1 pt-[74px]">
        {/* Hero Header */}
        <div className="relative py-16 md:py-20 px-6 overflow-hidden bg-gradient-to-b from-slate-50 via-white to-white dark:from-slate-900 dark:via-slate-950 dark:to-slate-950 border-b border-slate-200/80 dark:border-slate-800/80">
          {/* Back to Home Button */}
          <div className="max-w-7xl mx-auto mb-6">
            <Link
              to="/"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-300 hover:text-[#4B98C8] hover:border-[#4B98C8]/40 hover:bg-[#4B98C8]/5 hover:shadow-md transition-all duration-300 font-bold text-sm group"
            >
              <FiArrowLeft className="w-4 h-4 transition-transform group-hover:-translate-x-1 text-[#4B98C8]" />
              Back to Home
            </Link>
          </div>

          {/* Background Ambient Glows */}
          <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[600px] h-[350px] bg-gradient-to-br from-[#4B98C8]/15 to-[#205E85]/5 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute bottom-0 right-0 w-[400px] h-[300px] bg-[#4B98C8]/10 rounded-full blur-3xl pointer-events-none" />

          <div className="relative z-10 max-w-4xl mx-auto text-center">
            <h1 className="text-4xl sm:text-5xl md:text-6xl font-black text-slate-900 dark:text-white tracking-tight leading-[1.15] mb-5">
              Chapter{" "}
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#4B98C8] via-[#357ea9] to-[#205E85] dark:from-[#60a5fa] dark:to-[#4B98C8]">
                Rules
              </span>
            </h1>

            <p className="text-base sm:text-lg md:text-xl text-slate-600 dark:text-slate-300 font-normal max-w-2xl mx-auto leading-relaxed">
              Guiding principles, collaborative values, and standards of
              excellence for the ACM Alexandria Student Chapter community.
            </p>
          </div>
        </div>

        {/* Content Container */}
        <div className="max-w-7xl mx-auto px-6 py-14 space-y-16">
          {/* SECTION 1: CODE OF CONDUCT (ALWAYS VISIBLE) */}
          <section id="code-of-conduct">
            <div className="mb-8 pb-5 border-b border-slate-200 dark:border-slate-800">
              <h2 className="text-2xl sm:text-3xl font-black text-slate-900 dark:text-white tracking-tight">
                Code of Conduct
              </h2>
            </div>

            {/* 4 Pillars Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 lg:gap-8">
              {/* Commitment & Reliability */}
              <div className="rounded-3xl p-7 bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 hover:border-[#4B98C8]/50 dark:hover:border-[#4B98C8]/50 shadow-sm hover:shadow-xl hover:shadow-[#4B98C8]/10 transition-all duration-300">
                <div className="flex items-start gap-5">
                  <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-[#4B98C8]/20 to-[#205E85]/20 dark:from-[#4B98C8]/30 dark:to-[#205E85]/30 flex items-center justify-center shrink-0 text-[#205E85] dark:text-[#4B98C8] border border-[#4B98C8]/30">
                    <FiClock className="w-7 h-7" />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-xl font-black text-slate-900 dark:text-white mb-2.5">
                      Commitment & Reliability
                    </h3>
                    <p className="text-slate-600 dark:text-slate-300 text-sm leading-relaxed">
                      We value everyone's time. Members are expected to be
                      punctual for meetings, honor their task deadlines, and
                      actively participate in chapter activities.
                    </p>
                  </div>
                </div>
              </div>

              {/* Proactive Communication */}
              <div className="rounded-3xl p-7 bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 hover:border-[#4B98C8]/50 dark:hover:border-[#4B98C8]/50 shadow-sm hover:shadow-xl hover:shadow-[#4B98C8]/10 transition-all duration-300">
                <div className="flex items-start gap-5">
                  <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-sky-500/20 to-blue-600/20 dark:from-sky-500/30 dark:to-blue-600/30 flex items-center justify-center shrink-0 text-[#205E85] dark:text-[#4B98C8] border border-sky-400/30">
                    <FiMessageSquare className="w-7 h-7" />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-xl font-black text-slate-900 dark:text-white mb-2.5">
                      Proactive Communication
                    </h3>
                    <p className="text-slate-600 dark:text-slate-300 text-sm leading-relaxed">
                      Transparency is key to teamwork. If a member cannot meet a
                      deadline or attend a meeting, we expect them to communicate
                      with their team leads in advance.
                    </p>
                  </div>
                </div>
              </div>

              {/* Shared Responsibility */}
              <div className="rounded-3xl p-7 bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 hover:border-[#4B98C8]/50 dark:hover:border-[#4B98C8]/50 shadow-sm hover:shadow-xl hover:shadow-[#4B98C8]/10 transition-all duration-300">
                <div className="flex items-start gap-5">
                  <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-indigo-500/20 to-blue-700/20 dark:from-indigo-500/30 dark:to-blue-700/30 flex items-center justify-center shrink-0 text-[#205E85] dark:text-[#4B98C8] border border-indigo-400/30">
                    <FiUsers className="w-7 h-7" />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-xl font-black text-slate-900 dark:text-white mb-2.5">
                      Shared Responsibility
                    </h3>
                    <p className="text-slate-600 dark:text-slate-300 text-sm leading-relaxed">
                      We thrive on collaboration. In team projects, roles must be
                      clearly defined, and credit is shared fairly among all
                      contributors.
                    </p>
                  </div>
                </div>
              </div>

              {/* Professionalism & Respect */}
              <div className="rounded-3xl p-7 bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 hover:border-[#4B98C8]/50 dark:hover:border-[#4B98C8]/50 shadow-sm hover:shadow-xl hover:shadow-[#4B98C8]/10 transition-all duration-300">
                <div className="flex items-start gap-5">
                  <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-teal-500/20 to-emerald-600/20 dark:from-teal-500/30 dark:to-emerald-600/30 flex items-center justify-center shrink-0 text-[#205E85] dark:text-[#4B98C8] border border-teal-400/30">
                    <FiShield className="w-7 h-7" />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-xl font-black text-slate-900 dark:text-white mb-2.5">
                      Professionalism & Respect
                    </h3>
                    <p className="text-slate-600 dark:text-slate-300 text-sm leading-relaxed">
                      We foster a supportive and inclusive environment. Every
                      member is expected to treat their peers with kindness,
                      respect, and professionalism at all times.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </section>

          {/* SECTION 2: INTERNAL MEMBER POLICIES & GUIDELINES (SHOWN ONLY TO MEMBERS AND ABOVE) */}
          {isMemberOrAbove && (
            <section id="internal-policies" className="space-y-10 pt-4">
              <div className="pb-5 border-b border-slate-200 dark:border-slate-800">
                <h2 className="text-2xl sm:text-3xl font-black text-slate-900 dark:text-white tracking-tight">
                  Internal Member Policies & Guidelines
                </h2>
              </div>

              {/* 1. Task Management */}
              <div className="rounded-3xl p-7 md:p-8 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-6">
                <div className="flex items-center gap-3.5 pb-4 border-b border-slate-100 dark:border-slate-800">
                  <div className="w-11 h-11 rounded-xl bg-blue-500/10 text-[#4B98C8] flex items-center justify-center font-bold">
                    <FiCheckSquare className="w-5 h-5" />
                  </div>
                  <h3 className="text-2xl font-black text-slate-900 dark:text-white">
                    1. Task Management
                  </h3>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                  {/* Deadlines */}
                  <div className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/80 space-y-2.5">
                    <h4 className="font-black text-slate-900 dark:text-white text-base">
                      Deadlines
                    </h4>
                    <p className="text-slate-600 dark:text-slate-300 text-xs sm:text-sm leading-relaxed">
                      Tasks must be submitted by the agreed-upon deadline.
                      Submitting a task late without a pre-communicated excuse will
                      result in a{" "}
                      <strong className="text-rose-600 dark:text-rose-400">
                        Formal Warning
                      </strong>
                      .
                    </p>
                  </div>

                  {/* Task Execution */}
                  <div className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/80 space-y-2.5">
                    <h4 className="font-black text-slate-900 dark:text-white text-base">
                      Task Execution
                    </h4>
                    <p className="text-slate-600 dark:text-slate-300 text-xs sm:text-sm leading-relaxed">
                      Tasks must be completed accurately and according to the
                      provided instructions. Incorrect execution due to negligence
                      will result in a{" "}
                      <strong className="text-amber-600 dark:text-amber-400">
                        Verbal Warning
                      </strong>{" "}
                      (a formal heads-up).
                    </p>
                  </div>

                  {/* Co-Management Reporting */}
                  <div className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/80 space-y-2.5">
                    <h4 className="font-black text-slate-900 dark:text-white text-base">
                      Co-Management Reporting
                    </h4>
                    <p className="text-slate-600 dark:text-slate-300 text-xs sm:text-sm leading-relaxed">
                      For any project managed by two or more individuals, the
                      exact distribution of responsibilities must be reported to
                      the HR committee. Failure to submit this distribution will
                      result in a{" "}
                      <strong className="text-amber-600 dark:text-amber-400">
                        Verbal Warning
                      </strong>{" "}
                      for the managers involved.
                    </p>
                  </div>
                </div>
              </div>

              {/* 2. Attendance & Communication */}
              <div className="rounded-3xl p-7 md:p-8 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-6">
                <div className="flex items-center gap-3.5 pb-4 border-b border-slate-100 dark:border-slate-800">
                  <div className="w-11 h-11 rounded-xl bg-sky-500/10 text-sky-500 flex items-center justify-center font-bold">
                    <FiClock className="w-5 h-5" />
                  </div>
                  <h3 className="text-2xl font-black text-slate-900 dark:text-white">
                    2. Attendance & Communication
                  </h3>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  {/* Punctuality & Attendance */}
                  <div className="p-6 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/80 space-y-3">
                    <h4 className="font-black text-slate-900 dark:text-white text-lg">
                      Punctuality & Attendance
                    </h4>
                    <p className="text-slate-600 dark:text-slate-300 text-sm leading-relaxed">
                      Members are required to attend scheduled meetings and arrive
                      on time. Unexcused absences or failure to commit to meeting
                      times will result in a{" "}
                      <strong className="text-rose-600 dark:text-rose-400">
                        Formal Warning
                      </strong>
                      .
                    </p>
                  </div>

                  {/* Ghosting */}
                  <div className="p-6 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/80 space-y-3">
                    <h4 className="font-black text-slate-900 dark:text-white text-lg">
                      Ghosting
                    </h4>
                    <p className="text-slate-600 dark:text-slate-300 text-sm leading-relaxed">
                      Ignoring communications, abandoning accepted tasks, or
                      taking a silent absence without a proper reason disrupts
                      community operations. Any form of ghosting will result in a{" "}
                      <strong className="text-rose-600 dark:text-rose-400">
                        Formal Warning
                      </strong>
                      .
                    </p>
                  </div>
                </div>
              </div>

              {/* 3. Warning & Filtration System */}
              <div className="rounded-3xl p-7 md:p-8 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-6">
                <div className="flex items-center gap-3.5 pb-4 border-b border-slate-100 dark:border-slate-800">
                  <div className="w-11 h-11 rounded-xl bg-amber-500/10 text-amber-500 flex items-center justify-center font-bold">
                    <FiAlertTriangle className="w-5 h-5" />
                  </div>
                  <h3 className="text-2xl font-black text-slate-900 dark:text-white">
                    3. Warning & Filtration System
                  </h3>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                  {/* Verbal Warning */}
                  <div className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/80 space-y-2">
                    <div className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full bg-amber-500" />
                      <h4 className="font-black text-slate-900 dark:text-white text-base">
                        Verbal Warning
                      </h4>
                    </div>
                    <p className="text-slate-600 dark:text-slate-300 text-xs sm:text-sm leading-relaxed">
                      Issued as a formal notice for minor infractions (e.g.,
                      incorrect task execution or reporting failures).
                      Accumulating two (2) Verbal Warnings equates to one (1)
                      Formal Warning.
                    </p>
                  </div>

                  {/* Formal Warning */}
                  <div className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/80 space-y-2">
                    <div className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full bg-rose-500" />
                      <h4 className="font-black text-slate-900 dark:text-white text-base">
                        Formal Warning
                      </h4>
                    </div>
                    <p className="text-slate-600 dark:text-slate-300 text-xs sm:text-sm leading-relaxed">
                      Issued for direct policy breaches regarding deadlines,
                      attendance, and communication.
                    </p>
                  </div>

                  {/* Filtration */}
                  <div className="p-5 rounded-2xl bg-rose-50/60 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/60 space-y-2">
                    <div className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full bg-rose-600 animate-pulse" />
                      <h4 className="font-black text-rose-900 dark:text-rose-200 text-base">
                        Filtration
                      </h4>
                    </div>
                    <p className="text-rose-800 dark:text-rose-300 text-xs sm:text-sm leading-relaxed">
                      Accumulating three (3) Formal Warnings will result in
                      immediate filtration from the chapter.
                    </p>
                  </div>

                  {/* Warning Deduction */}
                  <div className="p-5 rounded-2xl bg-emerald-50/60 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900/60 space-y-2 md:col-span-2 lg:col-span-1">
                    <div className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
                      <h4 className="font-black text-emerald-900 dark:text-emerald-200 text-base">
                        Warning Deduction
                      </h4>
                    </div>
                    <p className="text-emerald-800 dark:text-emerald-300 text-xs sm:text-sm leading-relaxed">
                      Demonstrating outstanding performance or exceptional effort
                      can reduce a member’s record by one (1) Formal Warning.
                    </p>
                  </div>

                  {/* Behavioral Violations */}
                  <div className="p-5 rounded-2xl bg-amber-50/60 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-900/60 space-y-2 md:col-span-2">
                    <div className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full bg-amber-600" />
                      <h4 className="font-black text-amber-900 dark:text-amber-200 text-base">
                        Behavioral Violations
                      </h4>
                    </div>
                    <p className="text-amber-800 dark:text-amber-300 text-xs sm:text-sm leading-relaxed">
                      Any unethical or inappropriate behavioral actions bypass the
                      standard warning system and are subject to immediate
                      escalation.
                    </p>
                  </div>
                </div>
              </div>

              {/* 4. Performance Tracking & Recognition */}
              <div className="rounded-3xl p-7 md:p-8 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-6">
                <div className="flex items-center gap-3.5 pb-4 border-b border-slate-100 dark:border-slate-800">
                  <div className="w-11 h-11 rounded-xl bg-indigo-500/10 text-indigo-500 flex items-center justify-center font-bold">
                    <FiAward className="w-5 h-5" />
                  </div>
                  <h3 className="text-2xl font-black text-slate-900 dark:text-white">
                    4. Performance Tracking & Recognition
                  </h3>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Point System */}
                  <div className="p-6 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/80 space-y-3">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-[#4B98C8] flex items-center justify-center shrink-0">
                        <FiTrendingUp className="w-5 h-5" />
                      </div>
                      <h4 className="font-black text-slate-900 dark:text-white text-lg">
                        Point System
                      </h4>
                    </div>
                    <p className="text-slate-600 dark:text-slate-300 text-sm leading-relaxed">
                      Member performance is tracked through a point system.
                      Points are collected by completing tasks on time and
                      maintaining consistent engagement.
                    </p>
                  </div>

                  {/* Best Member */}
                  <div className="p-6 rounded-2xl bg-gradient-to-br from-amber-500/10 via-amber-500/5 to-transparent dark:from-amber-500/20 dark:via-slate-800 dark:to-slate-800 border border-amber-300/80 dark:border-amber-500/30 space-y-3">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-amber-500/20 text-amber-600 dark:text-amber-400 flex items-center justify-center shrink-0">
                        <FiAward className="w-5 h-5" />
                      </div>
                      <h4 className="font-black text-slate-900 dark:text-white text-lg">
                        Best Member
                      </h4>
                    </div>
                    <p className="text-slate-600 dark:text-slate-300 text-sm leading-relaxed">
                      Increased participation and active engagement yield higher
                      point totals. The members with the highest accumulated
                      points will earn the{" "}
                      <strong className="text-amber-600 dark:text-amber-400">
                        "Best Member"
                      </strong>{" "}
                      title.
                    </p>
                  </div>
                </div>
              </div>
            </section>
          )}

          {/* Bottom Note & Back Home */}
          <div className="p-6 md:p-8 rounded-3xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200 dark:border-slate-800 flex flex-col md:flex-row items-center justify-between gap-6 text-center md:text-left">
            <div>
              <h4 className="font-black text-slate-900 dark:text-white text-lg">
                Have questions regarding chapter rules?
              </h4>
              <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">
                Reach out to your committee leaders or the ACM Alexandria HR
                Board for clarifications.
              </p>
            </div>
            <Link
              to="/"
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#4B98C8] text-white font-bold text-sm hover:bg-[#3d86b3] transition-colors shadow-sm shrink-0"
            >
              Back to Home
              <FiChevronRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default RulesPage;
