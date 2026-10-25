import { useInView } from "../../hooks/useInView";

// Fades and lifts its children in once, the first time they scroll into view.
const Reveal = ({ as = "div", delay = 0, className = "", style, children }) => {
  const [ref, seen] = useInView();
  const Tag = as;
  return (
    <Tag
      ref={ref}
      style={{ transitionDelay: `${delay}ms`, ...style }}
      className={`transition-all duration-700 ease-out motion-reduce:transition-none ${seen ? "translate-y-0 opacity-100" : "translate-y-4 opacity-0 motion-reduce:translate-y-0 motion-reduce:opacity-100"} ${className}`}
    >
      {children}
    </Tag>
  );
};

export default Reveal;
