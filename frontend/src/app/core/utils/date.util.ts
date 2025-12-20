/**
 * Date utility functions
 */
export class DateUtil {
  /**
   * Format date to readable string
   */
  static formatDate(date: string | Date, format: 'short' | 'long' | 'date' = 'short'): string {
    const d = typeof date === 'string' ? new Date(date) : date;
    
    if (isNaN(d.getTime())) {
      return '-';
    }

    switch (format) {
      case 'long':
        return d.toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      case 'date':
        return d.toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        });
      default:
        return d.toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'short',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
    }
  }

  /**
   * Get relative time (e.g., "2 hours ago")
   */
  static getRelativeTime(date: string | Date): string {
    const d = typeof date === 'string' ? new Date(date) : date;
    const now = new Date();
    const diff = now.getTime() - d.getTime();
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) {
      return `${days} day${days > 1 ? 's' : ''} ago`;
    } else if (hours > 0) {
      return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    } else if (minutes > 0) {
      return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    } else {
      return 'Just now';
    }
  }

  /**
   * Check if date is in the past
   */
  static isPast(date: string | Date): boolean {
    const d = typeof date === 'string' ? new Date(date) : date;
    return d.getTime() < new Date().getTime();
  }

  /**
   * Check if date is in the future
   */
  static isFuture(date: string | Date): boolean {
    const d = typeof date === 'string' ? new Date(date) : date;
    return d.getTime() > new Date().getTime();
  }

  /**
   * Format date for input field (YYYY-MM-DD)
   */
  static formatForInput(date: string | Date): string {
    const d = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(d.getTime())) {
      return '';
    }
    return d.toISOString().split('T')[0];
  }

  /**
   * Format datetime for input field (YYYY-MM-DDTHH:mm)
   */
  static formatDateTimeForInput(date: string | Date): string {
    const d = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(d.getTime())) {
      return '';
    }
    return d.toISOString().slice(0, 16);
  }
}

