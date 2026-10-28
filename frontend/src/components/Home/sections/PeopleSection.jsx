import { Link } from "react-router-dom";
import { FiArrowRight } from "react-icons/fi";
import logo from "../../../assets/logo/acm-logo-no-bg.png";
import OrgTree from "../../ui/OrgTree";
import Portrait from "../../ui/Portrait";
import Reveal from "../../ui/Reveal";
import TeamsExplorer from "./TeamsExplorer";
import { muted, skeleton } from "../../ui/uiStyles";

// The chapter's people: the high board as a photo tree, then every committee and club with its own heads.
const PeopleSection = ({ loading, highBoard, committees, clubs, currentSeason, myCommitteeId, onApply, onShowClub, isRegistrationModalOpen }) => {
  const groups = {
    committees: committees.map((c) => ({ ...c, image: c.logoUrl })),
    clubs: clubs.map((c) => ({ ...c, image: c.imageUrl })),
  };

  return (
    <section id="people" className="mx-auto max-w-[1280px] scroll-mt-20 px-5 py-16 sm:px-8 sm:py-24">
      <Reveal className="mb-10 grid gap-6 sm:mb-14 lg:grid-cols-[1fr_auto] lg:items-end">
        <div>
          <h2 className="text-4xl font-semibold tracking-tight text-slate-900 sm:text-5xl dark:text-white" style={{ textWrap: "balance" }}>Led by students, for students</h2>
          <p className={`mt-4 max-w-2xl text-lg ${muted}`}>The high board leads the chapter. Every committee and club has its own heads under it.</p>
        </div>
        <Link to="/boards" className="inline-flex items-center gap-1.5 font-semibold text-ocean-573 hover:text-ocean-676 dark:text-ocean-316">See past boards <FiArrowRight className="h-4 w-4" aria-hidden="true" /></Link>
      </Reveal>

      {loading ? (
        <div className="grid grid-cols-2 gap-5 sm:grid-cols-4">
          {[0, 1, 2, 3].map((i) => <div key={i} className={`aspect-[4/5] rounded-[1.75rem] ${skeleton}`} />)}
        </div>
      ) : (
        <>
          {highBoard.length > 0 && (
            <OrgTree
              className="mb-14 sm:mb-20"
              members={highBoard}
              root={(
                <div className="flex items-center gap-4 rounded-full bg-ocean-794 py-2 pl-2 pr-6 text-white shadow-[0_18px_40px_-20px_color-mix(in_srgb,var(--color-ocean-794)_80%,transparent)] dark:bg-ocean-820">
                  <span className="flex h-12 w-12 items-center justify-center rounded-full bg-white"><img src={logo} alt="" className="h-9 w-9 object-contain" /></span>
                  <div className="text-left"><p className="text-base font-semibold">High board</p><p className="text-xs text-ocean-145">ACM Alexandria · {currentSeason ? `${currentSeason} season` : "this year"}</p></div>
                </div>
              )}
              renderMember={(m) => <Portrait member={m} aspect="aspect-[4/5]" />}
            />
          )}

          <Reveal>
            <TeamsExplorer groups={groups} myCommitteeId={myCommitteeId} onApply={onApply} onShowClub={onShowClub} isRegistrationModalOpen={isRegistrationModalOpen} />
          </Reveal>
        </>
      )}
    </section>
  );
};

export default PeopleSection;
