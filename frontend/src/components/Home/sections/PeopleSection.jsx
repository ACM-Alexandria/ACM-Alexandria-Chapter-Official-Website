import { Link } from "react-router-dom";
import { FiArrowRight } from "react-icons/fi";
import OrgTree from "../../ui/OrgTree";
import Portrait from "../../ui/Portrait";
import Reveal from "../../ui/Reveal";
import TeamTree, { TreeRoot } from "./TeamTree";
import { muted, primaryButton, sectionLead, sectionTitle, sectionY, skeleton } from "../../ui/uiStyles";

// The chapter's people: the high board as a photo tree, then every committee and club with its own heads.
const PeopleSection = ({ loading, highBoard, committees, clubs, currentSeason, myCommitteeId, onApply, onShowClub, isRegistrationModalOpen }) => {
  const groups = {
    committees: committees.map((c) => ({ ...c, image: c.logoUrl })),
    clubs: clubs.map((c) => ({ ...c, image: c.imageUrl })),
  };

  const season = currentSeason ? `${currentSeason} season` : "This year";
  const teamSection = (id, title, text, teams, committee) => teams.length > 0 && (
    <section id={id} className={`mx-auto max-w-[1280px] scroll-mt-20 px-5 sm:px-8 ${sectionY} !pt-0`}>
      <Reveal className="mb-8 max-w-2xl sm:mb-10">
        <h2 className={sectionTitle}>{title}</h2>
        <p className={`mt-3 sm:mt-4 ${sectionLead} ${muted}`}>{text}</p>
      </Reveal>
      <Reveal>
        <TeamTree
          root={<TreeRoot title={committee ? "Committees" : "Clubs"} subtitle={season} />}
          teams={teams}
          committee={committee}
          myCommitteeId={myCommitteeId}
          onApply={onApply}
          onShowClub={onShowClub}
          isRegistrationModalOpen={isRegistrationModalOpen}
        />
      </Reveal>
    </section>
  );

  return (
    <>
      <section id="people" className={`mx-auto max-w-[1280px] scroll-mt-20 px-5 sm:px-8 ${sectionY}`}>
        <Reveal className="mb-8 grid gap-5 sm:mb-12 lg:grid-cols-[1fr_auto] lg:items-end">
          <div>
            <h2 className={sectionTitle} style={{ textWrap: "balance" }}>Led by students, for students</h2>
            <p className={`mt-3 max-w-2xl sm:mt-4 ${sectionLead} ${muted}`}>The high board leads the chapter. Every committee and club has its own heads under it.</p>
          </div>
          <Link to="/boards" className={`${primaryButton} shrink-0`}>See past boards <FiArrowRight className="h-4 w-4" aria-hidden="true" /></Link>
        </Reveal>

        {loading ? (
          <div className="grid grid-cols-2 gap-5 sm:grid-cols-4">
            {[0, 1, 2, 3].map((i) => <div key={i} className={`aspect-[4/5] rounded-[1.75rem] ${skeleton}`} />)}
          </div>
        ) : highBoard.length > 0 && (
          <OrgTree
            members={highBoard}
            root={<TreeRoot title="High board" subtitle={season} />}
            renderMember={(m) => <Portrait member={m} aspect="aspect-[4/5]" />}
          />
        )}
      </section>

      {!loading && teamSection("committees", "Committees", "Each committee runs one part of the chapter. Apply while its call is open, or follow it for updates.", groups.committees, true)}
      {!loading && teamSection("clubs", "Clubs", "Tech communities for learning and building together.", groups.clubs, false)}
    </>
  );
};

export default PeopleSection;
