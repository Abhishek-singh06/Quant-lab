# License & Intellectual Property Audit: FinceptTerminal

**Reference Project**: [Fincept-Corporation/FinceptTerminal](https://github.com/Fincept-Corporation/FinceptTerminal)  
**Target Project**: QuantLab (`E:/VS CODE MAIN/PROJECTS/Quant-lab`)  
**Audit Date**: September 2026  
**Auditor**: QuantLab Architecture & Compliance Team  

---

## 1. Executive Licensing Declaration

> [!IMPORTANT]
> **Quant-lab does not copy, vendor, or import FinceptTerminal source code, binaries, or assets.**  
> QuantLab is an independently engineered quantitative research and market intelligence platform for Indian Equities. All code in QuantLab is written from scratch as clean-room implementations using standard enterprise frameworks (Java Spring Boot, Python FastAPI/NumPy/Pandas/Scikit-Learn, React/TypeScript/Tailwind CSS).

---

## 2. License Inspection & Findings

| Attribute | FinceptTerminal Open Source Edition | FinceptTerminal Enterprise Edition | QuantLab Implementation |
| :--- | :--- | :--- | :--- |
| **License Type** | **GNU Affero General Public License v3.0 (AGPL-3.0-or-later)** | Proprietary / Closed Source | **Independent Clean-Room Architecture** |
| **Source Availability** | Public repository on GitHub | Closed source (Private binary distributions) | Internal / Private Repository |
| **License Obligations** | Strong copyleft: any network deployment or distributed derivative work must disclose source under AGPL-3.0. | Commercial license fee ($10–$40/user/month). | No copyleft contamination; zero AGPL dependencies included. |
| **Language / Framework** | C++20, Qt6, embedded Python 3.11 desktop binary. | C++20, Qt6, proprietary backend. | Python 3.11+ (FastAPI), Java 21 (Spring Boot 3), TypeScript (React 18 / Vite). |
| **Trademarks & Trade Dress** | "Fincept", "Fincept Terminal", and logos are registered trademarks of Fincept Corporation. | Protected proprietary marks. | QuantLab branding, independent UX design, no trademark infringement. |

---

## 3. Strict IP & Clean-Room Boundaries

To ensure absolute compliance and prevent license contamination:

1. **Zero Source Code Copying**: No `.cpp`, `.h`, `.py`, `.qml`, or `.ui` files from `Fincept-Corporation/FinceptTerminal` are incorporated into QuantLab.
2. **Zero Dependency Linkage**: FinceptTerminal is not added as a submodule, Maven dependency, PyPI package, or npm package.
3. **No Visual Identity / Trademark Infringement**: QuantLab does not use Fincept's name, icons, trade dress, or logos. QuantLab utilizes its own custom dark-mode design system with Tailwind CSS and Lucide React icons.
4. **Architectural Study Only**: FinceptTerminal is studied purely at a conceptual level (e.g. multi-panel workspace composition, information density in market dashboards, news/events timeline presentation). All components in QuantLab are developed natively to interface with QuantLab's existing PostgreSQL PIT warehouse and service layer.

---

## 4. Conclusion & Legal Clearance

The FinceptTerminal repository is governed by AGPL-3.0 for its public desktop client. Because QuantLab:
- Does not link against or incorporate any FinceptTerminal source code or libraries,
- Uses an independent client-server web architecture (React + Java + Python) rather than a Qt6 desktop binary,
- Uses native clean-room data models tailored to Indian markets (NSE/BSE, SEBI, PIT timestamps),

**There is zero AGPL copyleft liability, copyright infringement, or license risk in QuantLab.**
