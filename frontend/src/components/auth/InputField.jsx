import { useState } from "react";
import { ErrorExclamationIcon } from "../icons/index";

const InputField = ({
  label,
  type = "text",
  name,
  value,
  onChange,
  onBlur,
  error = "",
  placeholder = "",
  required = false,
  autoComplete = "off",
  icon: Icon,
  maxLength,
}) => {
  const [isFocused, setIsFocused] = useState(false);

  return (
    <div className="mb-5">
      <label htmlFor={name} className="mb-1.5 block text-sm font-semibold text-slate-800 dark:text-slate-100">
        {label}
        {required && <span className="text-red-500 ml-1 font-bold">*</span>}
      </label>

      <div className="relative">
        {Icon && (
          <div className={`absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none transition-colors duration-200 ${isFocused ? "text-ocean-573 dark:text-ocean-316" : "text-slate-400"}`}>
            <Icon />
          </div>
        )}

        <input
          type={type}
          id={name}
          name={name}
          value={value}
          onChange={onChange}
          onBlur={(e) => {
            setIsFocused(false);
            if (onBlur) onBlur(e);
          }}
          onFocus={() => setIsFocused(true)}
          placeholder={placeholder}
          autoComplete={autoComplete}
          maxLength={maxLength}
          className={`
            w-full pr-4 ${Icon ? "pl-11" : "pl-4"}
            h-12 rounded-xl bg-white text-[15px] text-slate-900 placeholder:text-slate-400 outline-none transition-shadow duration-200
            dark:bg-ocean-911 dark:text-slate-100 dark:placeholder:text-slate-500
            ${error
              ? "ring-2 ring-red-400 dark:ring-red-500/70"
              : isFocused
                ? "ring-2 ring-ocean-461"
                : "ring-1 ring-ocean-106 hover:ring-ocean-255 dark:ring-white/15 dark:hover:ring-white/25"
            }
          `}
          aria-invalid={error ? "true" : "false"}
          aria-describedby={error ? `${name}-error` : undefined}
        />
      </div>

      {error && (
        <p
          id={`${name}-error`}
          className="mt-1.5 flex items-center gap-1 text-xs font-medium text-red-600 dark:text-red-400"
          role="alert"
          style={{ animation: "fadeIn 0.2s ease both" }}
        >
          <ErrorExclamationIcon />
          {error}
        </p>
      )}
    </div>
  );
};

export default InputField;
