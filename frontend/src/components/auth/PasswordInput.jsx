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
    <div className="mb-6">
      <div className="flex justify-between items-center mb-1.5">
        <label htmlFor={name} className="block text-sm font-bold text-gray-700">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
        {/* Forgot Password Link Inline */}
        {forgotPasswordLink && (
          <div className="text-xs font-bold text-gray-700 hover:text-blue-600 transition-colors">
            {forgotPasswordLink}
          </div>
        )}
      </div>

      <div className="relative">
        {/* Render Left Icon (Lock) */}
        {Icon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
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
          // Padding logic: Left padding depends on Icon, Right padding always reserved for Eye button
          className={`w-full py-3 pr-12 ${Icon ? "pl-10" : "px-4"} border rounded-lg focus:outline-none focus:ring-2 transition-all duration-200 placeholder-gray-300 ${
            error
              ? "border-red-500 focus:ring-red-500"
              : isFocused
                ? "border-blue-400 focus:ring-blue-100"
                : "border-gray-200 focus:ring-blue-100"
          }`}
          aria-invalid={error ? "true" : "false"}
          aria-describedby={error ? `${name}-error` : undefined}
        />

        {/* Eye Toggle Button */}
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 focus:outline-none transition-colors duration-200"
          tabIndex={0}>
          {showPassword ? <EyeSlashIcon /> : <EyeIcon />}
        </button>
      </div>

      {error && (
        <p
          id={`${name}-error`}
          className="mt-1.5 text-xs text-red-600 flex items-center gap-1"
          role="alert">
          <ErrorExclamationIcon />
          {error}
        </p>
      )}
    </div>
  );
};

export default PasswordInput;
