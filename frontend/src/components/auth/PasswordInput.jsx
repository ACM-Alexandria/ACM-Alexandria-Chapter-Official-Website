import { useState } from "react";
import { EyeSlashIcon, EyeIcon, ErrorExclamationIcon } from "../icons/index";

const PasswordInput = ({
  label = "Password",
  name = "password",
  value,
  onChange,
  onBlur,
  error = "",
  placeholder = "Enter your password",
  required = false,
  autoComplete = "current-password",
  icon: Icon,
  forgotPasswordLink,
  maxLength,
}) => {
  const [showPassword, setShowPassword] = useState(false);
  const [isFocused, setIsFocused] = useState(false);

  return (
    <div className="mb-5">
      <div className="mb-1.5 flex items-center justify-between">
        <label htmlFor={name} className="block text-sm font-semibold text-slate-800 dark:text-slate-100">
          {label}
          {required && <span className="text-red-500 ml-1 font-bold">*</span>}
        </label>
        {forgotPasswordLink && (
          <span className="text-sm font-semibold text-[#2A7AB0] transition-colors hover:text-[#205E85] dark:text-[#7DBBE0] dark:hover:text-white">
            {forgotPasswordLink}
          </span>
        )}
      </div>

      <div className="relative">
        {Icon && (
          <div className={`absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none transition-colors duration-200 ${isFocused ? "text-[#2A7AB0] dark:text-[#7DBBE0]" : "text-slate-400"}`}>
            <Icon />
          </div>
        )}

        <input
          type={showPassword ? "text" : "password"}
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
            w-full pr-12 ${Icon ? "pl-11" : "pl-4"}
            h-12 rounded-xl bg-white text-[15px] text-slate-900 placeholder:text-slate-400 outline-none transition-shadow duration-200
            dark:bg-[#0A1724] dark:text-slate-100 dark:placeholder:text-slate-500
            ${error
              ? "ring-2 ring-red-400 dark:ring-red-500/70"
              : isFocused
                ? "ring-2 ring-[#4B98C8]"
                : "ring-1 ring-[#D6E6F2] hover:ring-[#9CC5E0] dark:ring-white/15 dark:hover:ring-white/25"
            }
          `}
          aria-invalid={error ? "true" : "false"}
          aria-describedby={error ? `${name}-error` : undefined}
        />

        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-[#205E85] dark:hover:text-[#7DBBE0] transition-colors duration-200 focus:outline-none p-0.5 rounded"
          tabIndex={0}
          aria-label={showPassword ? "Hide password" : "Show password"}
        >
          {showPassword ? <EyeSlashIcon /> : <EyeIcon />}
        </button>
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

export default PasswordInput;
