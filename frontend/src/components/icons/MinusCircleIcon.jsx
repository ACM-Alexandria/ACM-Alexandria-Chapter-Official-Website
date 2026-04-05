/** Circle with minus/dash — rule not-passed indicator */
const MinusCircleIcon = ({ className = "w-3 h-3", ...props }) => (
  <svg className={className} fill="currentColor" viewBox="0 0 20 20" {...props}>
    <path
      fillRule="evenodd"
      d="M10 18a8 8 0 100-16 8 8 0 000 16zM7 9a1 1 0 000 2h6a1 1 0 100-2H7z"
      clipRule="evenodd"
    />
  </svg>
);

export default MinusCircleIcon;
