import api from "./api";

/**
 * Event Review Service
 * Backend (EventReviewController), base path: /api/event-reviews
 *   POST  /add               { eventId, rating, comment? }   -> 201   (registered attendees only)
 *   POST  /summary           { eventId }                     -> { eventId, averageRating, reviewCount }
 *   POST  /comments/public   { eventId }                     -> [{ comment }]
 *   POST  /comments          { eventId }                     -> [{ comment }]              (admin)
 *   POST  /admin             { eventId }                     -> [{ reviewId, rating, comment, status }]
 *   POST  /mine              { eventId }                     -> { reviewed: boolean }
 *   PATCH /visibility        { reviewId }                    -> { reviewId, status }       (admin)
 */

const BASE = "/api/event-reviews";

const toError = (error, fallback) => {
  const status = error.response?.status;
  const data = error.response?.data;
  const serverMessage =
    data?.error ||
    data?.message ||
    (typeof data === "string" ? data : null);

  let message = serverMessage;
  if (!message) {
    if (status === 401) message = "Please log in to continue.";
    else if (status === 403) message = "Only registered attendees can leave feedback.";
    else if (status === 409) message = "You already reviewed this event.";
    else message = fallback;
  }

  const err = new Error(message);
  err.status = status;
  return err;
};

export const addReview = async ({ eventId, rating, comment }) => {
  try {
    const body = { eventId, rating };
    if (comment && comment.trim()) body.comment = comment.trim();
    await api.post(`${BASE}/add`, body);
  } catch (error) {
    throw toError(error, "Could not submit your review. Please try again.");
  }
};

/** Returns { eventId, averageRating, reviewCount } */
export const getRatingSummary = async (eventId) => {
  try {
    const response = await api.post(`${BASE}/summary`, { eventId });
    return response.data;
  } catch (error) {
    throw toError(error, "Could not load the rating.");
  }
};

/** Returns [{ comment }] */
export const getPublicComments = async (eventId) => {
  try {
    const response = await api.post(`${BASE}/comments/public`, { eventId });
    return response.data;
  } catch (error) {
    throw toError(error, "Could not load comments.");
  }
};

/** Returns [{ comment }] — admin only */
export const getAllComments = async (eventId) => {
  try {
    const response = await api.post(`${BASE}/comments`, { eventId });
    return response.data;
  } catch (error) {
    throw toError(error, "Could not load comments.");
  }
};

/** Returns [{ reviewId, rating, comment, status }] — admin only, anonymous */
export const getAdminReviews = async (eventId) => {
  try {
    const response = await api.post(`${BASE}/admin`, { eventId });
    return response.data;
  } catch (error) {
    throw toError(error, "Could not load reviews.");
  }
};

/** Returns whether the current user already reviewed this event */
export const hasUserReviewed = async (eventId) => {
  try {
    const response = await api.post(`${BASE}/mine`, { eventId });
    return Boolean(response.data?.reviewed);
  } catch (error) {
    throw toError(error, "Could not check your review status.");
  }
};

/** Returns { reviewId, status } */
export const toggleReviewVisibility = async (reviewId) => {
  try {
    const response = await api.patch(`${BASE}/visibility`, { reviewId });
    return response.data;
  } catch (error) {
    throw toError(error, "Could not change visibility.");
  }
};
