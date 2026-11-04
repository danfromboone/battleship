Name:           fleetstrike
Version:        1.0.0
Release:        1%{?dist}
Summary:        Fleet Strike - Tactical naval warfare game
Group:          Amusements/Games
License:        MIT
URL:            https://github.com/danfromboone/fleetstrike
Source0:        %{name}-%{version}.tar.gz
BuildArch:      noarch

Requires:       java-headless >= 1:21

%description
Fleet Strike is a tactical naval combat strategy game built with Java 21 adhering
to Domain-Driven Design (DDD) principles. Features both a rich Swing GUI
presentation layer with interactive radar targeting, fleet placement hover
previews, pass-and-play hotseat privacy protection, and smart Hunt-and-Target AI,
as well as a full-featured ANSI console terminal interface.

%prep
%autosetup

%build
# Pre-built using Gradle

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}%{_bindir}
mkdir -p %{buildroot}%{_datadir}/%{name}/bin
mkdir -p %{buildroot}%{_datadir}/%{name}/lib
mkdir -p %{buildroot}%{_datadir}/applications
mkdir -p %{buildroot}%{_datadir}/icons/hicolor/scalable/apps
mkdir -p %{buildroot}%{_datadir}/pixmaps
mkdir -p %{buildroot}%{_docdir}/%{name}

# Application files
install -m 755 build/install/fleetstrike/bin/fleetstrike %{buildroot}%{_datadir}/%{name}/bin/fleetstrike
install -m 644 build/install/fleetstrike/lib/fleetstrike-%{version}.jar %{buildroot}%{_datadir}/%{name}/lib/

# Executable symlink in /usr/bin
ln -sf %{_datadir}/%{name}/bin/fleetstrike %{buildroot}%{_bindir}/%{name}

# Desktop entry & icons
install -m 644 packaging/debian/fleetstrike.desktop %{buildroot}%{_datadir}/applications/%{name}.desktop
install -m 644 packaging/debian/fleetstrike.svg %{buildroot}%{_datadir}/icons/hicolor/scalable/apps/%{name}.svg
install -m 644 packaging/debian/fleetstrike.svg %{buildroot}%{_datadir}/pixmaps/%{name}.svg

# Documentation
install -m 644 README.md %{buildroot}%{_docdir}/%{name}/
install -m 644 packaging/debian/copyright %{buildroot}%{_docdir}/%{name}/

%files
%{_bindir}/%{name}
%{_datadir}/%{name}
%{_datadir}/applications/%{name}.desktop
%{_datadir}/icons/hicolor/scalable/apps/%{name}.svg
%{_datadir}/pixmaps/%{name}.svg
%doc %{_docdir}/%{name}

%changelog
* Wed Nov 04 2026 Dan <danfromboone@gmail.com> - 1.0.0-1
- Initial release: Fleet Strike with Console UI and Swing GUI presentation layer
