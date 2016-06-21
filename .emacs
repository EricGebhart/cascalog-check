(defvar mypackages '(cider
                     clojure-mode
                     flycheck-clojure
                     flycheck-pos-tip))

;;
;; Install stuff from packages.

(defun install-mypackages ()
  (dolist (pkg mypackages)
    (unless (package-installed-p pkg))
    (package-install pkg)))

(defun update-mypackages ()
  (interactive)
  (dolist (pkg mypackages)
    (package-install pkg)))

(require 'package)
(add-to-list 'package-archives '("melpa" . "http://melpa.milkbox.net/packages/") t)
(add-to-list 'package-archives '("melpa-stable" . "http://stable.melpa.org/packages/"))
(add-to-list 'package-archives '("gnu" . "http://elpa.gnu.org/packages/") t)
(add-to-list 'package-archives '("org" . "http://orgmode.org/elpa/") t)
(add-to-list 'package-archives '("marmalade" . "http://marmalade-repo.org/packages/") t)
(package-initialize)
(when (not package-archive-contents)
  (package-refresh-contents))
;;(defvar foo (package-list-packages))

(install-mypackages)
;;(update-mypackages)


;;;squiggly-clojure.
(require 'flycheck-pos-tip)

(eval-after-load 'flycheck '(flycheck-clojure-setup))
(add-hook 'after-init-hook #'global-flycheck-mode)

(with-eval-after-load 'flycheck
   (flycheck-pos-tip-mode))

(eval-after-load 'flycheck
  '(setq flycheck-display-errors-function #'flycheck-pos-tip-error-messages))
