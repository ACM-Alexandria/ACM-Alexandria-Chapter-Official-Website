import React, { createElement } from "react";
import {
  HiStar,
  HiOutlineExternalLink,
  HiOutlinePhone,
  HiOutlineAcademicCap,
  HiOutlineCalendar,
  HiOutlineLibrary
} from "react-icons/hi";
import { FaLinkedinIn } from "react-icons/fa";
import logo from "../../assets/logo/acm-logo-no-bg.png";
import { DEPARTMENT_LABELS, getRoleLabel, canShowPhoto } from "./profileUtils";

// Strip protocol, "www." and LinkedIn tracking params so only the readable part shows.
const displayUrl = (url) => url.replace(/^https?:\/\/(www\.)?/, "").split(/[?#]/)[0].replace(/\/$/, "");

const AVATAR_SIZES = {
  sm: "w-12 h-12 text-lg",
  lg: "w-20 h-20 text-2xl",
  xl: "w-24 h-24 sm:w-28 sm:h-28 text-3xl ring-4 ring-white dark:ring-slate-900",
};

export const ProfileAvatar = ({ profile, email, size = "lg" }) => {
  const sizeClass = AVATAR_SIZES[size];
  const initial = (profile.name || email || "U")[0].toUpperCase();

  if (profile.profileImageUrl && canShowPhoto(profile.role)) {
    return (
      <img
        src={profile.profileImageUrl}
        alt=""
        className={`${sizeClass} rounded-full object-cover border border-slate-200 dark:border-ocean-745 shrink-0`}
      />
    );
  }

  return (
    <div
      aria-hidden="true"
      className={`${sizeClass} rounded-full bg-ocean-57 dark:bg-ocean-808 text-ocean-614 dark:text-ocean-316 font-semibold flex items-center justify-center shrink-0`}
    >
      {initial}
    </div>
  );
};

// Brand blue in light mode is darkened slightly so tag text keeps AA contrast.
const Tag = ({ children }) => (
  <span className="inline-flex items-center px-3 py-1 rounded-full bg-ocean-461/10 border border-ocean-461/30 text-ocean-614 dark:text-ocean-316 text-xs font-semibold">
    {children}
  </span>
);

export const ProfileHeader = ({ user, profile, actions }) => (
  <header className="rounded-xl border border-slate-200 dark:border-ocean-800 bg-white dark:bg-ocean-890 overflow-hidden">
    {/* Cover: soft brand tint with the chapter logo as a watermark */}
    <div className="relative h-24 sm:h-28 overflow-hidden bg-ocean-57 dark:bg-ocean-808 border-b border-ocean-461/15">
      <img
        src={logo}
        alt=""
        aria-hidden="true"
        className="absolute -right-4 -top-6 h-40 sm:h-48 w-auto opacity-[0.12] dark:opacity-[0.15] select-none pointer-events-none"
      />
    </div>

    <div className="px-5 sm:px-6 pb-6">
      <div className="relative flex items-end justify-between gap-4 -mt-12 sm:-mt-14">
        <ProfileAvatar profile={profile} email={user?.email} size="xl" />
        {actions && <div className="flex items-center gap-2 pb-1">{actions}</div>}
      </div>

      <h1 className="mt-4 text-2xl md:text-3xl font-semibold text-slate-900 dark:text-slate-100 tracking-tight break-words [text-wrap:balance]">
        {profile.name || "Unnamed member"}
      </h1>
      <p className="mt-1 text-sm text-slate-600 dark:text-slate-400 break-all">{user?.email}</p>

      <div className="mt-4 flex flex-wrap items-center gap-2">
        <Tag>{getRoleLabel(profile.role)}</Tag>
        {profile.committeeName && <Tag>{profile.committeeName}</Tag>}
      </div>

      {profile.role !== "USER" && (
        <p className="mt-3 flex items-center gap-1.5 text-sm text-slate-600 dark:text-slate-400">
          <HiStar className="w-[18px] h-[18px] text-yellow-400" aria-hidden="true" />
          <span className="font-semibold text-slate-900 dark:text-slate-100 tabular-nums">
            {profile.seasonPoints || 0}
          </span>
          season points
        </p>
      )}
    </div>
  </header>
);

const DetailTile = ({ icon, label, children }) => (
  <div
    className={`flex items-start gap-3 rounded-lg border border-slate-200 dark:border-ocean-800 bg-slate-50/60 dark:bg-ocean-916/60 p-4`}
  >
    <span className="w-10 h-10 shrink-0 rounded-lg bg-ocean-461/10 text-ocean-614 dark:text-ocean-316 flex items-center justify-center">
      {createElement(icon, { className: "w-5 h-5", "aria-hidden": true })}
    </span>
    <div className="min-w-0 flex-1 pt-0.5">
      <dt className="text-xs text-slate-600 dark:text-slate-400">{label}</dt>
      <dd className="mt-0.5 text-sm font-medium text-slate-900 dark:text-slate-100 break-words">{children}</dd>
    </div>
  </div>
);

const Missing = ({ children = "Not added" }) => (
  <span className="font-normal text-slate-500 dark:text-slate-400">{children}</span>
);

export const ProfileDetails = ({ profile }) => {
  const isStudent = profile.isAlexEngStudent === true;

  return (
    <dl className="p-4 sm:p-5 flex flex-col gap-3">
      {/* Row 1: contact */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <DetailTile icon={HiOutlinePhone} label="Phone number">
          {profile.phoneNumber ? <span className="tabular-nums">{profile.phoneNumber}</span> : <Missing />}
        </DetailTile>

        <DetailTile icon={FaLinkedinIn} label="LinkedIn">
          {profile.linkedinUrl ? (
            <a
              href={profile.linkedinUrl}
              target="_blank"
              rel="noreferrer"
              title={profile.linkedinUrl}
              className="flex max-w-full items-center gap-1 text-ocean-461 hover:underline underline-offset-2"
            >
              <span className="truncate">{displayUrl(profile.linkedinUrl)}</span>
              <HiOutlineExternalLink className="w-4 h-4 shrink-0" aria-hidden="true" />
            </a>
          ) : (
            <Missing />
          )}
        </DetailTile>
      </div>

      {/* Row 2: university */}
      <div className={`grid grid-cols-1 gap-3 ${isStudent ? "md:grid-cols-3" : ""}`}>
        <DetailTile icon={HiOutlineAcademicCap} label="Alexandria Engineering">
          {profile.isAlexEngStudent === null || profile.isAlexEngStudent === undefined ? (
            <Missing>Not specified</Missing>
          ) : isStudent ? (
            "Student"
          ) : (
            "Not a student"
          )}
        </DetailTile>

        {isStudent && (
          <>
            <DetailTile icon={HiOutlineCalendar} label="Batch">{profile.batch || <Missing />}</DetailTile>
            <DetailTile icon={HiOutlineLibrary} label="Department">
              {DEPARTMENT_LABELS[profile.department] || <Missing />}
            </DetailTile>
          </>
        )}
      </div>
    </dl>
  );
};

export const Panel = ({ title, aside, children }) => (
  <section className="rounded-xl border border-slate-200 dark:border-ocean-800 bg-white dark:bg-ocean-890 overflow-hidden">
    {(title || aside) && (
      <div className="px-5 py-4 flex items-center justify-between gap-4 border-b border-slate-200 dark:border-ocean-800">
        <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">{title}</h2>
        {aside}
      </div>
    )}
    {children}
  </section>
);

const ProfileViewCard = ({ user, profile, actions }) => (
  <div className="flex flex-col gap-8">
    <ProfileHeader user={user} profile={profile} actions={actions} />
    <Panel title="Details">
      <ProfileDetails profile={profile} />
    </Panel>
  </div>
);

export default ProfileViewCard;
