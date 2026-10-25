import React, { useState, useEffect, useRef } from "react";
import IslandNav from "../components/Layout/IslandNav";
import { siteSections } from "../components/Layout/siteSections";
import adminService from "../services/adminService";
import { useAuth } from "../contexts/useAuth";
import {
  fetchHighBoard,
  fetchSeasons,
} from "../services/homePageService";
import InsightsTab from "../components/AdminPage/InsightsSection/InsightsTab";
import ManagementSidebar from "../components/AdminPage/ManagementSection/ManagementSidebar";
import ResourceTable from "../components/AdminPage/ManagementSection/ResourceTable";
import ResourceFormModal from "../components/AdminPage/ManagementSection/ResourceFormModal";
import ClubSocialsModal from "../components/AdminPage/ManagementSection/ClubSocialsModal";
import EpisodesManagementModal from "../components/AdminPage/ManagementSection/EpisodesManagementModal";
import EventGalleryModal from "../components/AdminPage/ManagementSection/EventGalleryModal";
import OpenCallConfirmModal from "../components/AdminPage/ManagementSection/OpenCallConfirmModal";
import { Highlight } from "../components/ThemedDialog";
import useThemedDialog from "../hooks/useThemedDialog";
import FeedbackTab from "../components/AdminPage/ManagementSection/FeedbackTab";
import HRFeedbackTab from "../components/AdminPage/ManagementSection/HRFeedbackTab";
import UserManagementTab from "../components/AdminPage/ManagementSection/UserManagementTab";
import GalleryTab from "../components/AdminPage/ManagementSection/GalleryTab";
import PastBoardsTab from "../components/AdminPage/PastBoardsSection/PastBoardsTab";
import EmailCenterTab from "../components/AdminPage/EmailCenterSection/EmailCenterTab";
import AdminPageHeader from "../components/AdminPage/SharedComponents/AdminPageHeader";
import AdminTabs from "../components/AdminPage/SharedComponents/AdminTabs";
import ResourceToolbar from "../components/AdminPage/SharedComponents/ResourceToolbar";
import { fieldClass, noticeClass, secondaryButtonClass } from "../components/AdminPage/SharedComponents/adminStyles";
import ResourceDialogs from "../components/AdminPage/ManagementSection/ResourceDialogs";
import ResourcePagination from "../components/AdminPage/SharedComponents/ResourcePagination";
import useResourceDialogs from "../hooks/useResourceDialogs";
import useEmailLock from "../hooks/useEmailLock";
import { loadResourceList } from "../hooks/useResourceList";
import { SHARED_RESOURCE_TABS, resourceTypeForTab } from "../constants/resourceTypes";
import {
  FiCalendar,
  FiAward,
  FiBookOpen,
  FiUserCheck,
  FiLayers,
  FiFileText,
  FiRefreshCw,
  FiTrendingUp,
  FiSliders,
  FiActivity,
  FiPlus,
  FiSearch,
  FiChevronLeft,
  FiChevronRight,
  FiAlertTriangle,
  FiShare2,
  FiGrid,
  FiRadio,
  FiMessageSquare,
  FiGlobe,
  FiMail,
  FiLock,
} from "react-icons/fi";

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   ADMIN PAGE
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
const AdminPage = () => {
  const [activeTab, setActiveTab] = useState("insights");

  // ── Resource Management States ──
  const [mgmtTab, setMgmtTab] = useState("users");
  const [mgmtSearchQuery, setMgmtSearchQuery] = useState("");
  const [mgmtLoading, setMgmtLoading] = useState(false);
  const [mgmtError, setMgmtError] = useState(null);

  // Data states
  const [highBoard, setHighBoard] = useState([]);
  const [committees, setCommittees] = useState([]);
  const [events, setEvents] = useState({ content: [], number: 0, totalPages: 1 });
  const [clubs, setClubs] = useState({ content: [], number: 0, totalPages: 1 });
  const [programs, setPrograms] = useState({ content: [], number: 0, totalPages: 1 });
  const [seasons, setSeasons] = useState({ content: [], number: 0, totalPages: 1 });
  const [exclusiveForms, setExclusiveForms] = useState([]);
  const [socialLinks, setSocialLinks] = useState([]);
  const [partners, setPartners] = useState([]);

  // Episodes management modal states
  const [episodesModalOpen, setEpisodesModalOpen] = useState(false);
  const [selectedSeasonForEpisodes, setSelectedSeasonForEpisodes] = useState(null);

  // Club Socials modal states
  const [socialsModalOpen, setSocialsModalOpen] = useState(false);
  const [selectedClubForSocials, setSelectedClubForSocials] = useState(null);

  // Committee Board Member Specifics
  const [selectedCommitteeId, setSelectedCommitteeId] = useState("");

  // Modal / Form states
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState("add"); // 'add' or 'edit'
  const [editingItem, setEditingItem] = useState(null);
  const [formData, setFormData] = useState({});

  const [modalError, setModalError] = useState(null);

  // Event Gallery modal states
  const [galleryModalOpen, setGalleryModalOpen] = useState(false);
  const [selectedEventForGallery, setSelectedEventForGallery] = useState(null);

  // Committee open-call confirm states
  const [openCallCommittee, setOpenCallCommittee] = useState(null);

  // Double-click guards for email-sending actions (refs update immediately, unlike state)
  const announceInFlight = useRef(false);
  const openCallInFlight = useRef(false);
  const [announcingId, setAnnouncingId] = useState(null);

  // Site-wide email lock (only SUPER_ADMIN can change it)
  const { user } = useAuth();
  // Themed confirmations, errors and hints for the email actions (same look as the unsubscribe dialog)
  const { dialog, confirm, notify } = useThemedDialog();
  const isSuperAdmin = user?.role === "SUPER_ADMIN";
  // Past boards can be managed by every board role; only archiving the season stays super-admin only
  const canManagePastBoards = ["SUPER_ADMIN", "ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"].includes(user?.role);
  const [emailsEnabled, setEmailsEnabled] = useEmailLock();
  const [emailLockSaving, setEmailLockSaving] = useState(false);

  // HR Feedback is visible to the super admin and to the HR Committee board only
  const canSeeHRFeedback =
    isSuperAdmin || (user?.role === "ACM_COMMITTEE_BOARD" && user?.committeeName === "HR Committee");

  const mgmtTabs = [
    ...SHARED_RESOURCE_TABS.slice(0, 5),
    { id: "radio", label: "Radio", icon: FiRadio },
    ...SHARED_RESOURCE_TABS.slice(5),
    { id: "gallery", label: "Gallery", icon: FiGrid },
    { id: "socialLinks", label: "Social Links", icon: FiShare2 },
    { id: "partners", label: "Partners", icon: FiGlobe },
    { id: "feedback", label: "Grow Feedback", icon: FiMessageSquare },
    { id: "hrFeedback", label: "HR Feedback", icon: FiMessageSquare },
    ...(canManagePastBoards ? [{ id: "pastBoards", label: "Past Boards", icon: FiLayers }] : []),
  ].filter((tab) => tab.id !== "hrFeedback" || canSeeHRFeedback);

  const loadMgmtTabData = async (tab, page = 0) => {
    setMgmtLoading(true);
    setMgmtError(null);
    try {
      if (tab === "users") {
        // Users are fetched inside UserManagementTab
      } else if (tab === "highboard") {
        const data = await fetchHighBoard();
        setHighBoard(data.sort((a, b) => (a.order || 99) - (b.order || 99)));

      } else if (tab === "committees" || tab === "committeeBoard") {
        const sortedData = await loadResourceList("committees");
        setCommittees(sortedData);
        if (sortedData.length > 0 && !selectedCommitteeId) {
          setSelectedCommitteeId(sortedData[0].id.toString());
        }

      } else if (tab === "events") {
        setEvents(await loadResourceList("events", page));
      } else if (tab === "clubs") {
        setClubs(await loadResourceList("clubs", page));
      } else if (tab === "programs") {
        setPrograms(await loadResourceList("programs", page));
      } else if (tab === "radio") {
        const data = await fetchSeasons(page);
        const sorted = {
          ...data,
          content: (data.content || []).sort((a, b) => b.seasonNumber - a.seasonNumber),
        };
        setSeasons(sorted);
      } else if (tab === "exclusiveForms") {
        setExclusiveForms(await loadResourceList("exclusiveForms"));
      } else if (tab === "socialLinks") {
        const data = await adminService.fetchSocialLinks();
        setSocialLinks(data);
      } else if (tab === "partners") {
        const data = await adminService.fetchPartners();
        setPartners(data || []);
      } else if (tab === "feedback") {
        // Handled internally in FeedbackTab
      } else if (tab === "hrFeedback") {
        // Handled internally in HRFeedbackTab
      }
    } catch (err) {
      console.error(err);
      setMgmtError("Failed to fetch resource data.");
    } finally {
      setMgmtLoading(false);
    }
  };

  const dialogs = useResourceDialogs({ onChanged: () => loadMgmtTabData(mgmtTab) });

  useEffect(() => {
    if (activeTab === "management") {
      loadMgmtTabData(mgmtTab);
    }
  }, [activeTab, mgmtTab]);

  const getFilteredMgmtData = () => {
    const q = mgmtSearchQuery.toLowerCase();
    if (mgmtTab === "highboard") {
      return highBoard.filter(
        (m) =>
          (m.name && m.name.toLowerCase().includes(q)) ||
          (m.role && m.role.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "committees") {
      return committees.filter(
        (c) =>
          c.name.toLowerCase().includes(q) ||
          (c.description && c.description.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "committeeBoard") {
      const comm = committees.find((c) => c.id.toString() === selectedCommitteeId);
      if (!comm || !comm.boardRoles) return [];
      return comm.boardRoles.filter(
        (m) =>
          (m.name && m.name.toLowerCase().includes(q)) ||
          (m.role && m.role.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "events") {
      return events.content.filter(
        (e) =>
          e.name.toLowerCase().includes(q) ||
          (e.description && e.description.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "clubs") {
      return clubs.content.filter(
        (c) =>
          c.name.toLowerCase().includes(q) ||
          (c.description && c.description.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "programs") {
      return programs.content.filter(
        (p) =>
          p.name.toLowerCase().includes(q) ||
          (p.description && p.description.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "radio") {
      return seasons.content.filter(
        (s) =>
          s.seasonNumber.toString().includes(q)
      );
    } else if (mgmtTab === "exclusiveForms") {
      return exclusiveForms.filter(
        (f) =>
          (f.title && f.title.toLowerCase().includes(q)) ||
          (f.description && f.description.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "socialLinks") {
      return socialLinks.filter(
        (sl) =>
          sl.platform.toLowerCase().includes(q) ||
          sl.url.toLowerCase().includes(q)
      );
    } else if (mgmtTab === "partners") {
      return partners.filter(
        (p) =>
          p.name.toLowerCase().includes(q) ||
          (p.description && p.description.toLowerCase().includes(q))
      );
    }
    return [];
  };

  const handleMgmtAddClick = () => {
    setFormMode("add");
    setEditingItem(null);
    setMgmtError(null);
    setModalError(null);

    if (mgmtTab === "highboard") {
      setFormData({ name: "", role: "", imageUrl: "", order: null, linkedinUrl: "" });
    } else if (mgmtTab === "committees") {
      setFormData({ name: "", description: "", logoUrl: "" });
    } else if (mgmtTab === "committeeBoard") {
      setFormData({ name: "", role: "", imageUrl: "", order: null, linkedinUrl: "" });
    } else if (mgmtTab === "events") {
      setFormData({ name: "", description: "", imageUrl: "", eventTime: "", endTime: "", location: "", attachedImages: [], registrationOpen: false, sendAnnouncement: true });
    } else if (mgmtTab === "clubs") {
      setFormData({ name: "", description: "", imageUrl: "", isExternal: false, registrationOpen: false, sendAnnouncement: true });
    } else if (mgmtTab === "programs") {
      setFormData({ name: "", description: "", imageUrl: "", startDate: "", endDate: "", time: "", registrationOpen: false, sendAnnouncement: true });
    } else if (mgmtTab === "radio") {
      setFormData({ seasonNumber: "", imageUrl: "" });
    } else if (mgmtTab === "exclusiveForms") {
      setFormData({ title: "", description: "", imageUrl: "", isActive: true });
    } else if (mgmtTab === "socialLinks") {
      setFormData({ platform: "", url: "" });
    } else if (mgmtTab === "partners") {
      setFormData({ name: "", website: "", imageUrl: "" });
    }
    setFormOpen(true);
  };

  const handleMgmtEditClick = (item) => {
    setFormMode("edit");
    setEditingItem(item);
    setMgmtError(null);
    setModalError(null);

    if (mgmtTab === "events" && item.eventTime) {
      const date = new Date(item.eventTime);
      const formattedDate = date.toISOString().slice(0, 16);
      const formattedEndDate = item.endTime
        ? new Date(item.endTime).toISOString().slice(0, 16)
        : "";

      setFormData({
        ...item,
        eventTime: formattedDate,
        endTime: formattedEndDate
      });
    } else if (mgmtTab === "programs") {
      const formattedStartDate = item.startDate ? new Date(item.startDate).toISOString().slice(0, 16) : "";
      const formattedEndDate = item.endDate ? new Date(item.endDate).toISOString().slice(0, 16) : "";
      setFormData({ ...item, startDate: formattedStartDate, endDate: formattedEndDate });
    } else if (mgmtTab === "committees") {
      // Only send fields relevant to the committee itself — boardRoles are managed separately
      const { boardRoles: _boardRoles, callMessage: _callMessage, topicToken: _topicToken, open: _open, ...committeeFields } = item;
      setFormData(committeeFields);
    } else if (mgmtTab === "highboard" || mgmtTab === "committeeBoard") {
      setFormData({
        ...item,
        name: item.user?.name || "",
        imageUrl: item.user?.profileImageUrl || "",
        linkedinUrl: item.user?.linkedinUrl || ""
      });
    } else {
      setFormData({ ...item });
    }
    setFormOpen(true);
  };

  const handleMgmtFormSubmit = async (e) => {
    e.preventDefault();

    // Inline Questions Validation
    if (mgmtTab === "events" && formMode === "add" && formData.questions) {
      for (const q of formData.questions) {
        if (!q.questionText || !q.questionText.trim()) {
          setModalError("Question text cannot be empty.");
          return;
        }
        if (q.questionType === "MULTIPLE_CHOICE" || q.questionType === "CHECKBOX") {
          const validOptions = (q.options || []).filter(opt => opt && opt.trim() !== "");
          if (validOptions.length === 0) {
            setModalError(`Question "${q.questionText}" must have at least one valid option.`);
            return;
          }
        }
      }
    }

    setMgmtLoading(true);
    setMgmtError(null);
    setModalError(null);

    try {
      let payload = { ...formData };
      if (mgmtTab === "highboard" && payload.user) {
        payload.user = {
          ...payload.user,
          name: payload.name !== undefined ? payload.name : payload.user.name,
          profileImageUrl: payload.imageUrl !== undefined ? payload.imageUrl : payload.user.profileImageUrl,
          linkedinUrl: payload.linkedinUrl !== undefined ? payload.linkedinUrl : payload.user.linkedinUrl
        };
      } else if (mgmtTab === "committeeBoard") {
        payload.userName = payload.name !== undefined ? payload.name : payload.userName;
        payload.profileImageUrl = payload.imageUrl !== undefined ? payload.imageUrl : payload.profileImageUrl;
        payload.linkedinUrl = payload.linkedinUrl !== undefined ? payload.linkedinUrl : payload.linkedinUrl;
      }

      if (mgmtTab === "highboard") {
        if (formMode === "add") {
          await adminService.addHighBoardMember(payload);
        } else {
          await adminService.updateHighBoardMember(editingItem.id, payload);
        }
      } else if (mgmtTab === "committees") {
        if (formMode === "add") {
          await adminService.createCommittee(formData);
        } else {
          await adminService.updateCommittee(editingItem.id, formData);
        }
      } else if (mgmtTab === "committeeBoard") {
        if (formMode === "add") {
          await adminService.addCommitteeBoardMember(parseInt(selectedCommitteeId), payload);
        } else {
          await adminService.updateCommitteeBoardMember(editingItem.id, payload);
        }
      } else if (mgmtTab === "events") {
        if (formMode === "add") {
          let newEvent = null;
          try {
            const { questions: _questions, ...eventPayload } = formData;
            newEvent = await adminService.createEvent(eventPayload);

            if (newEvent?.id && formData.questions && formData.questions.length > 0) {
              for (const q of formData.questions) {
                const payload = {
                  question_text: q.questionText,
                  question_type: q.questionType,
                  is_required: q.isRequired,
                  options: (q.questionType === "MULTIPLE_CHOICE" || q.questionType === "CHECKBOX") ? q.options : []
                };
                await adminService.createQuestion("event", newEvent.id, payload);
              }
            }
          } catch (error) {
            // Rollback the event if question creation failed
            if (newEvent?.id) {
              try {
                await adminService.deleteEvent(newEvent.id);
              } catch (rollbackError) {
                console.error("Failed to rollback event after question creation failure", rollbackError);
              }
            }
            throw error; // Let the outer catch handle the modal error state
          }

          setFormOpen(false);
          loadMgmtTabData(mgmtTab);
          return; // skip the generic setFormOpen(false) / loadMgmtTabData below
        } else {
          const { questions: _questions, ...eventPayload } = formData;
          await adminService.updateEvent(editingItem.id, eventPayload);
        }
      } else if (mgmtTab === "clubs") {
        if (formMode === "add") {
          await adminService.createClub(formData);
        } else {
          await adminService.updateClub(editingItem.id, formData);
        }
      } else if (mgmtTab === "programs") {
        if (formMode === "add") {
          await adminService.createProgram(formData);
        } else {
          await adminService.updateProgram(editingItem.id, formData);
        }
      } else if (mgmtTab === "radio") {
        if (formMode === "add") {
          await adminService.createSeason(formData);
        } else {
          await adminService.updateSeason(editingItem.id, formData);
        }
      } else if (mgmtTab === "exclusiveForms") {
        if (formMode === "add") {
          await adminService.createExclusiveForm(formData);
        } else {
          await adminService.updateExclusiveForm(editingItem.id, formData);
        }
      } else if (mgmtTab === "socialLinks") {
        if (formMode === "add") {
          await adminService.createSocialLink(formData);
        } else {
          await adminService.updateSocialLink(editingItem.id, formData);
        }
      } else if (mgmtTab === "partners") {
        if (formMode === "add") {
          await adminService.createPartner(formData);
        } else {
          await adminService.updatePartner(editingItem.id, formData);
        }
      }

      setFormOpen(false);
      loadMgmtTabData(mgmtTab);
    } catch (err) {
      console.error(err);
      const msg = err.message || err.error || (typeof err === "string" ? err : null) || `Failed to ${formMode} resource.`;
      setModalError(msg);
    } finally {
      setMgmtLoading(false);
    }
  };

  const handleMgmtDeleteClick = async (item) => {
    if (deleteInFlight.current) return;
    deleteInFlight.current = true;

    try {
      const itemName = item.name || (item.seasonNumber !== undefined ? `Season ${item.seasonNumber}` : "this item");

      // Dynamic cascade-aware description based on entity type
      let title = "Delete Record?";
      let description = (
        <>
          Are you sure you want to delete <Highlight>{itemName}</Highlight>? This action is permanent and cannot be undone.
        </>
      );

      if (mgmtTab === "radio") {
        title = "Delete Season and All Episodes?";
        const episodeCount = item.episodesCount ?? item.episodes?.length;
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently delete the season and{" "}
            {episodeCount !== undefined ? `all ${episodeCount} episodes` : "all episodes"}{" "}
            belonging to it. This action cannot be undone.
          </>
        );
      } else if (mgmtTab === "events") {
        title = "Delete Event?";
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently delete the event, its custom questions, and all attendee registrations. This action cannot be undone.
          </>
        );
      } else if (mgmtTab === "clubs") {
        title = "Delete Club?";
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently delete the club, its questions, and all club registrations. This action cannot be undone.
          </>
        );
      } else if (mgmtTab === "programs") {
        title = "Delete Program?";
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently delete the program, its questions, and all participant registrations. This action cannot be undone.
          </>
        );
      } else if (mgmtTab === "committees") {
        title = "Delete Committee?";
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently remove the committee, its board members, questions, call history, and subscriber list. This action cannot be undone.
          </>
        );
      } else if (mgmtTab === "exclusiveForms") {
        title = "Delete Exclusive Form?";
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently delete the form, all its configured questions, and submissions. This action cannot be undone.
          </>
        );
      }

      const confirmed = await confirm({
        tone: "danger",
        title,
        description,
        confirmLabel: "Yes, Delete",
        cancelLabel: "Cancel",
      });

      if (!confirmed) return;

      setMgmtLoading(true);
      setMgmtError(null);
      try {
        if (mgmtTab === "highboard") {
          await adminService.deleteHighBoardMember(item.id);
        } else if (mgmtTab === "committees") {
          await adminService.deleteCommittee(item.id);
        } else if (mgmtTab === "committeeBoard") {
          await adminService.deleteCommitteeBoardMember(item.id);
        } else if (mgmtTab === "events") {
          await adminService.deleteEvent(item.id);
        } else if (mgmtTab === "clubs") {
          await adminService.deleteClub(item.id);
        } else if (mgmtTab === "programs") {
          await adminService.deleteProgram(item.id);
        } else if (mgmtTab === "radio") {
          await adminService.deleteSeason(item.id);
        } else if (mgmtTab === "exclusiveForms") {
          await adminService.deleteExclusiveForm(item.id);
        } else if (mgmtTab === "socialLinks") {
          await adminService.deleteSocialLink(item.id);
        } else if (mgmtTab === "partners") {
          await adminService.deletePartner(item.id);
        }

        await loadMgmtTabData(mgmtTab);
      } catch (err) {
        console.error(err);
        const msg = err.message || err.error || (typeof err === "string" ? err : null) || "Failed to delete resource.";
        setMgmtError(msg);
      } finally {
        setMgmtLoading(false);
      }
    } finally {
      deleteInFlight.current = false;
    }
  };

  const handleEpisodesClick = (season) => {
    setSelectedSeasonForEpisodes(season);
    setEpisodesModalOpen(true);
  };

  const toggleInFlight = useRef(false);
  const deleteInFlight = useRef(false);

  const handleToggleCall = async (item) => {
    if (toggleInFlight.current) return;
    toggleInFlight.current = true;

    try {
      const isCurrentlyOpen =
        mgmtTab === "programs"
          ? Boolean(item.registrationOpen)
          : mgmtTab === "exclusiveForms"
            ? Boolean(item.isActive)
            : Boolean(item.registrationOpen || item.open || item.isOpen);

      const resourceName = item.name || item.title || "this resource";

      // If opening a committee call, preserve OpenCallConfirmModal to configure emailing subscribers
      if (mgmtTab === "committees" && !isCurrentlyOpen) {
        setOpenCallCommittee(item);
        return;
      }

      // Determine confirmation tone, title, and description based on resource type and action direction
      const isClosing = isCurrentlyOpen;
      let title = "";
      let description = "";
      let tone = isClosing ? "warning" : "brand";
      let confirmLabel = isClosing ? "Close" : "Open";

      if (mgmtTab === "committees") {
        title = "Close Committee Call?";
        description = (
          <>
            Closing this call for <Highlight>{resourceName}</Highlight> will immediately stop accepting new committee applications. Existing applications will remain safely stored, but new applicants will no longer be able to submit until the call is reopened.
          </>
        );
        confirmLabel = "Close Call";
      } else if (mgmtTab === "exclusiveForms") {
        title = isClosing ? "Deactivate Form?" : "Activate Form?";
        description = isClosing ? (
          <>
            Deactivating <Highlight>{resourceName}</Highlight> will immediately prevent new submissions. Existing submissions will remain saved.
          </>
        ) : (
          <>
            Activating <Highlight>{resourceName}</Highlight> will make the form publicly available and allow new applicants to submit entries.
          </>
        );
        confirmLabel = isClosing ? "Deactivate" : "Activate";
      } else {
        title = isClosing ? "Close Registration?" : "Open Registration?";
        description = isClosing ? (
          <>
            Closing registration for <Highlight>{resourceName}</Highlight> will immediately stop accepting new registrations. Existing registrations will remain preserved.
          </>
        ) : (
          <>
            Opening registration for <Highlight>{resourceName}</Highlight> will make the registration form publicly available and allow new applicants to register.
          </>
        );
        confirmLabel = isClosing ? "Close Registration" : "Open Registration";
      }

      const confirmed = await confirm({
        tone,
        title,
        description,
        confirmLabel,
        cancelLabel: "Cancel",
      });

      if (!confirmed) return;

      setMgmtLoading(true);
      setMgmtError(null);
      try {
        if (mgmtTab === "programs") {
          await adminService.toggleProgramRegistration(item.id, !isCurrentlyOpen);
        } else if (mgmtTab === "clubs") {
          if (isCurrentlyOpen) {
            await adminService.closeClubCall(item.id);
          } else {
            await adminService.openClubCall(item.id);
          }
        } else if (mgmtTab === "events") {
          if (isCurrentlyOpen) {
            await adminService.closeEventCall(item.id);
          } else {
            await adminService.openEventCall(item.id);
          }
        } else if (mgmtTab === "exclusiveForms") {
          await adminService.updateExclusiveForm(item.id, {
            ...item,
            isActive: !isCurrentlyOpen,
          });
        } else {
          await adminService.closeCommitteeCall(item.id);
        }
        await loadMgmtTabData(mgmtTab);
      } catch (err) {
        console.error(err);
        let errMsg = `Failed to update ${mgmtTab === "programs"
          ? "program registration"
          : mgmtTab === "exclusiveForms"
            ? "exclusive form status"
            : "committee call"
          } status.`;
        if (typeof err === "string") {
          errMsg = err;
        } else if (err && typeof err === "object") {
          errMsg = err.error || err.message || errMsg;
        }
        setMgmtError(errMsg);
      } finally {
        setMgmtLoading(false);
      }
    } finally {
      toggleInFlight.current = false;
    }
  };

  const handleConfirmOpenCall = async (sendAnnouncement) => {
    if (openCallInFlight.current) return;
    openCallInFlight.current = true;
    setMgmtLoading(true);
    setMgmtError(null);
    try {
      await adminService.openCommitteeCall(openCallCommittee.id, sendAnnouncement);
      setOpenCallCommittee(null);
      await loadMgmtTabData(mgmtTab);
    } catch (err) {
      console.error(err);
      setOpenCallCommittee(null);
      setMgmtError(err?.error || err?.message || (typeof err === "string" ? err : "Failed to open committee call."));
    } finally {
      openCallInFlight.current = false;
      setMgmtLoading(false);
    }
  };

  const handleAnnounceClick = async (item) => {
    // Claimed before the confirm prompt so a double-click can't open a second prompt or send twice
    if (announceInFlight.current) return;
    announceInFlight.current = true;
    const isCommittee = mgmtTab === "committees";
    const ok = await confirm({
      tone: "brand",
      title: isCommittee ? "Resend Call Email?" : "Send Announcement?",
      message: isCommittee
        ? <>Resend the <Highlight>{item.name}</Highlight> call email to its subscribers?</>
        : <>Send the <Highlight>{item.name}</Highlight> announcement email to all newsletter subscribers?</>,
      confirmLabel: "Yes, Send",
    });
    if (!ok) {
      announceInFlight.current = false;
      return;
    }

    setAnnouncingId(item.id);
    setMgmtLoading(true);
    setMgmtError(null);
    try {
      const sent = await adminService.sendWithResendPrompt(
        (force) =>
          isCommittee
            ? adminService.announceCommitteeCall(item.id, force)
            : adminService.sendAnnouncement(resourceTypeForTab(mgmtTab), item.id, force),
        confirm
      );
      if (sent) {
        notify({ tone: "success", title: "Emails On The Way", message: "The emails are being sent to subscribers." });
      }
    } catch (err) {
      console.error(err);
      notify({
        tone: "danger",
        title: "Couldn't Send",
        message: err?.error || err?.message || (typeof err === "string" ? err : "Failed to send announcement."),
      });
    } finally {
      announceInFlight.current = false;
      setAnnouncingId(null);
      setMgmtLoading(false);
    }
  };

  const emailLockInFlight = useRef(false);

  const handleToggleEmailLock = async () => {
    if (emailLockInFlight.current || emailLockSaving) return;
    emailLockInFlight.current = true;

    try {
      const enable = !emailsEnabled;
      if (!enable) {
        const ok = await confirm({
          tone: "danger",
          title: "Lock All Emails?",
          description: "No emails or notifications will be sent (except password resets) until you unlock them.",
          confirmLabel: "Yes, Lock",
          cancelLabel: "Cancel",
        });
        if (!ok) return;
      } else {
        const ok = await confirm({
          tone: "brand",
          title: "Enable System Emails?",
          description: "This will re-enable system-wide email delivery for notifications and other email operations.",
          confirmLabel: "Yes, Enable",
          cancelLabel: "Cancel",
        });
        if (!ok) return;
      }
      setEmailLockSaving(true);
      try {
        const data = await adminService.updateEmailLock(enable);
        setEmailsEnabled(data?.emailsEnabled !== false);
      } catch (err) {
        console.error(err);
        notify({ tone: "danger", title: "Couldn't Update", message: err?.message || err?.error || "Failed to update the email lock." });
      } finally {
        setEmailLockSaving(false);
      }
    } finally {
      emailLockInFlight.current = false;
    }
  };

  const handleSocialsClick = (item) => {
    setSelectedClubForSocials(item);
    setSocialsModalOpen(true);
  };

  const handleGalleryClick = (item) => {
    setSelectedEventForGallery(item);
    setGalleryModalOpen(true);
  };



  const tabs = [
    { id: "insights", label: "System Insights", shortLabel: "Insights", icon: FiTrendingUp },
    { id: "management", label: "Resource Management", shortLabel: "Resources", icon: FiSliders },
    { id: "email", label: "Email Center", shortLabel: "Email", icon: FiMail },
  ];

  // Paginated resources, keyed by tab
  const pagedData = { events, clubs, programs, radio: seasons };
  const pageInfo = pagedData[mgmtTab];

  const sectionNames = {
    highboard: "high board",
    committeeBoard: "committee board",
    radio: "radio seasons",
    socialLinks: "social links",
    exclusiveForms: "exclusive forms",
  };
  const sectionName = sectionNames[mgmtTab] || mgmtTab;

  const hasOwnHeader = ["users", "gallery", "feedback", "hrFeedback", "pastBoards"].includes(mgmtTab);

  return (
    <div className="min-h-screen bg-[#F3F7FB] dark:bg-[#0B1520] text-slate-800 dark:text-slate-100 font-sans">
      <IslandNav links={siteSections} home={false} solid />

      <main className="admin-ui pt-[106px] pb-20 px-4 sm:px-6 md:px-12 max-w-[1600px] mx-auto flex flex-col gap-6">
        <AdminPageHeader
          title="Dashboard"
          description="Administration"
          actions={
            <>
              {isSuperAdmin && (
                <button
                  type="button"
                  role="switch"
                  aria-checked={emailsEnabled}
                  onClick={handleToggleEmailLock}
                  disabled={emailLockSaving}
                  title={emailsEnabled ? "Lock all site emails" : "Unlock site emails"}
                  className={secondaryButtonClass}
                >
                  <FiLock className={`w-4 h-4 ${emailsEnabled ? "text-[#4B98C8]" : "text-rose-600"}`} aria-hidden="true" />
                  Site emails
                  <span
                    aria-hidden="true"
                    className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors ${emailsEnabled ? "bg-[#4B98C8]" : "bg-rose-500"}`}
                  >
                    <span
                      className={`inline-block h-3.5 w-3.5 transform rounded-full bg-white shadow transition-transform ${emailsEnabled ? "translate-x-4" : "translate-x-1"}`}
                    />
                  </span>
                </button>
              )}
            </>
          }
        />

        {/* Email lock warning */}
        {!emailsEnabled && (
          <div role="alert" className={`flex items-start gap-2.5 ${noticeClass.error}`}>
            <FiAlertTriangle className="w-5 h-5 shrink-0 mt-px" aria-hidden="true" />
            <p>
              All site emails are locked. No emails or notifications are sent (except password resets).
              {isSuperAdmin ? " Use the Site emails switch above to unlock." : " Only the super admin can unlock them."}
            </p>
          </div>
        )}

        <AdminTabs tabs={tabs} active={activeTab} onChange={setActiveTab} ariaLabel="Admin sections" />

        {activeTab === "insights" && (
          <InsightsTab />
        )}

        {activeTab === "email" && <EmailCenterTab />}

        {activeTab === "management" && (
          <div className="flex flex-col lg:flex-row gap-4 lg:gap-6 items-start">
            <ManagementSidebar
              activeTab={mgmtTab}
              setActiveTab={setMgmtTab}
              tabs={mgmtTabs}
              setSearchQuery={setMgmtSearchQuery}
            />

            <div className="flex-1 w-full min-w-0 rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-4 sm:p-6 min-h-[500px] flex flex-col justify-between">
              <div>
                {!hasOwnHeader && (
                  <ResourceToolbar
                    title={`Manage ${sectionName}`}
                    description="Add, edit or delete items in this section."
                    onAdd={handleMgmtAddClick}
                    search={mgmtSearchQuery}
                    onSearchChange={setMgmtSearchQuery}
                  >
                    {/* Committee selector for committee board management */}
                    {mgmtTab === "committeeBoard" && (
                      <select
                        aria-label="Committee"
                        value={selectedCommitteeId}
                        onChange={(e) => setSelectedCommitteeId(e.target.value)}
                        className={`${fieldClass} sm:w-auto`}
                      >
                        {committees.map((c) => (
                          <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                      </select>
                    )}
                  </ResourceToolbar>
                )}

                {mgmtError && (
                  <div role="alert" className={`mb-5 flex items-start gap-2.5 ${noticeClass.error}`}>
                    <FiAlertTriangle className="w-5 h-5 shrink-0 mt-px" aria-hidden="true" />
                    <p>{mgmtError}</p>
                  </div>
                )}

                {/* Users, gallery, feedback and past boards render their own UI */}
                {mgmtTab === "pastBoards" ? (
                  <PastBoardsTab />
                ) : mgmtTab === "users" ? (
                  <UserManagementTab />
                ) : mgmtTab === "gallery" ? (
                  <GalleryTab />
                ) : mgmtTab === "feedback" ? (
                  <FeedbackTab />
                ) : mgmtTab === "hrFeedback" && canSeeHRFeedback ? (
                  <HRFeedbackTab />
                ) : (
                  <ResourceTable
                    activeTab={mgmtTab}
                    filteredItems={getFilteredMgmtData()}
                    loading={mgmtLoading}
                    onEditClick={handleMgmtEditClick}
                    onDeleteClick={handleMgmtDeleteClick}
                    onToggleCall={handleToggleCall}
                    onEditMessageClick={dialogs.openCallMessage}
                    onRegistrationClick={(item) => dialogs.openRegistration(item, resourceTypeForTab(mgmtTab) ?? "committee")}
                    onQuestionsClick={(item) => dialogs.openQuestions(item, resourceTypeForTab(mgmtTab) ?? "committee")}
                    onSocialsClick={handleSocialsClick}
                    onGalleryClick={handleGalleryClick}
                    onEpisodesClick={handleEpisodesClick}
                    onAnnounceClick={handleAnnounceClick}
                    announcingId={announcingId}
                    onEmailsClick={(item) => dialogs.openEmails(item, resourceTypeForTab(mgmtTab))}
                  />
                )}
              </div>

              <ResourcePagination pageInfo={pageInfo} onPageChange={(page) => loadMgmtTabData(mgmtTab, page)} />
            </div>

            {/* Modals */}
            <ResourceFormModal
              open={formOpen}
              onClose={() => { setFormOpen(false); setModalError(null); }}
              onSubmit={handleMgmtFormSubmit}
              formMode={formMode}
              activeTab={mgmtTab}
              formData={formData}
              setFormData={setFormData}
              loading={mgmtLoading}
              error={modalError}
            />

            <ResourceDialogs dialogs={dialogs} />


            <ClubSocialsModal
              open={socialsModalOpen}
              onClose={() => { setSocialsModalOpen(false); setModalError(null); }}
              club={selectedClubForSocials}
              onSaved={() => loadMgmtTabData(mgmtTab)}
            />


            <EpisodesManagementModal
              open={episodesModalOpen}
              onClose={() => { setEpisodesModalOpen(false); setModalError(null); }}
              seasonId={selectedSeasonForEpisodes?.id}
              seasonNumber={selectedSeasonForEpisodes?.seasonNumber}
            />

            <EventGalleryModal
              open={galleryModalOpen}
              onClose={() => { setGalleryModalOpen(false); setSelectedEventForGallery(null); }}
              event={selectedEventForGallery}
              onSave={() => loadMgmtTabData(mgmtTab)}
            />


            <OpenCallConfirmModal
              open={Boolean(openCallCommittee)}
              onClose={() => setOpenCallCommittee(null)}
              onConfirm={handleConfirmOpenCall}
              committee={openCallCommittee}
              loading={mgmtLoading}
            />
          </div>
        )}
      </main>
      <div className="admin-ui">{dialog}</div>
    </div>
  );
};

export default AdminPage;
