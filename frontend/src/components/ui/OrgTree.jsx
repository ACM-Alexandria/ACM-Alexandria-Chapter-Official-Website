import { useInView } from "../../hooks/useInView";

const wideColumns = { 3: "sm:grid-cols-3", 4: "sm:grid-cols-4", 5: "sm:grid-cols-5", 6: "sm:grid-cols-6" };

// A root node with members branching underneath. The connector lines draw in once, when the tree
// scrolls into view. With more than two members the lines show from `sm` up, where all members fit
// on one row; on phones the members wrap into two columns without lines.
const OrgTree = ({ root, members, renderMember, lineClass = "bg-ocean-255 dark:bg-ocean-678", className = "" }) => {
  const [ref, seen] = useInView(0.25);
  const grow = (axis, delay) => (seen ? { animation: `hp-grow-${axis} 0.3s cubic-bezier(0.22,1,0.36,1) ${delay}s both` } : { transform: axis === "x" ? "scaleX(0)" : "scaleY(0)" });
  const count = members.length;
  const wide = count > 2;
  const edge = `${50 / count}%`;
  const columns = wide ? `grid-cols-2 ${wideColumns[count] || "sm:grid-cols-4"}` : "grid-cols-2";

  return (
    <div ref={ref} className={`flex flex-col items-center ${className}`}>
      {root}
      {count > 0 && (
        <>
          <span className={`h-10 w-px origin-top ${lineClass}`} style={grow("y", 0)} aria-hidden="true" />
          <ul className={`relative grid w-full gap-x-5 gap-y-6 pt-10 ${columns}`}>
            {count > 1 && <span className={`absolute top-0 h-px origin-center ${lineClass} ${wide ? "hidden sm:block" : ""}`} style={{ left: edge, right: edge, ...grow("x", 0.15) }} aria-hidden="true" />}
            {members.map((member, i) => (
              <li key={member.name} className="relative" style={seen ? { animation: `hp-rise 0.4s cubic-bezier(0.22,1,0.36,1) ${0.3 + i * 0.05}s both` } : { opacity: 0 }}>
                <span className={`absolute -top-10 left-1/2 h-10 w-px origin-top ${lineClass} ${wide ? "hidden sm:block" : ""}`} style={grow("y", 0.3)} aria-hidden="true" />
                {renderMember(member)}
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
};

export default OrgTree;
