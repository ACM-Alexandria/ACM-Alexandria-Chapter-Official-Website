import { useState } from "react";
import { EyeSlashIcon, EyeIcon, ErrorExclamationIcon } from "../icons";

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
}) => {
  const [showPassword, setShowPassword] = useState(false);
  const [isFocused, setIsFocused] = useState(false);

  return (
    <div className="mb-5">
      <div className="flex justify-between items-center mb-1.5 ml-1">
        <label htmlFor={name} className="block text-[13px] font-medium text-gray-600 uppercase tracking-wider">
          {label}
          {required && <span className="text-red-500 ml-1 font-bold">*</span>}
        </label>
        {forgotPasswordLink && (
          <span className="text-[13px] font-semibold text-[#4B98C8] hover:text-[#205E85] transition-colors">
            {forgotPasswordLink}
          </span>
        )}
      </div>

      <div className="relative">
        {Icon && (
          <div className={`absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none transition-colors duration-200 ${isFocused ? "text-[#4B98C8]" : "text-gray-400"}`}>
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
          className={`
            w-full py-2.5 pr-12 ${Icon ? "pl-10" : "pl-4"}
            bg-gray-50 border rounded-xl text-gray-800 text-sm placeholder-gray-300
            outline-none transition-all duration-200
            ${error
              ? "border-red-400 bg-red-50 focus:ring-2 focus:ring-red-200"
              : isFocused
                ? "border-[#4B98C8] bg-white ring-2 ring-[#4B98C8]/20"
                : "border-gray-200 hover:border-gray-300 focus:border-[#4B98C8] focus:bg-white focus:ring-2 focus:ring-[#4B98C8]/20"
            }
          `}
          aria-invalid={error ? "true" : "false"}
          aria-describedby={error ? `${name}-error` : undefined}
        />

        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-[#4B98C8] transition-colors duration-200 focus:outline-none p-0.5 rounded"
          tabIndex={0}
          aria-label={showPassword ? "Hide password" : "Show password"}
        >
          {showPassword ? <EyeSlashIcon /> : <EyeIcon />}
        </button>
      </div>

      {error && (
        <p
          id={`${name}-error`}
          className="mt-1.5 text-xs text-red-500 flex items-center gap-1"
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
