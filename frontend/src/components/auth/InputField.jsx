import { useState } from "react";
import { ErrorExclamationIcon } from "../icons"; 

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
}) => {
  const [isFocused, setIsFocused] = useState(false);

  return (
    <div className="mb-4">
      <label
        htmlFor={name}
        className="block text-sm font-bold text-gray-700 mb-1.5">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>

      <div className="relative">
        {/* Render Left Icon if provided */}
        {Icon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
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
          className={`w-full py-3 pr-4 ${Icon ? "pl-10" : "px-4"} border rounded-lg focus:outline-none focus:ring-2 transition-all duration-200 placeholder-gray-300 ${
            error
              ? "border-red-500 focus:ring-red-500"
              : isFocused
                ? "border-blue-400 focus:ring-blue-100" 
                : "border-gray-200 focus:ring-blue-100"
          }`}
          aria-invalid={error ? "true" : "false"}
          aria-describedby={error ? `${name}-error` : undefined}
        />
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

export default InputField;
