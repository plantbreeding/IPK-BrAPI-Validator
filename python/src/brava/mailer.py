"""HTML templates and emails."""

from __future__ import annotations

import logging
from collections.abc import Sequence
from email.message import EmailMessage
from email.utils import formataddr
from typing import Any

import aiosmtplib
from jinja2 import Environment, PackageLoader, select_autoescape

from brava.config import get_settings
from brava.db import Resource, TestReport
from brava.reports import SuiteReport, add_stats

log = logging.getLogger(__name__)

templates = Environment(loader=PackageLoader("brava", "templates"), autoescape=select_autoescape(["html"]))


def render(name: str, **context: Any) -> str:
    s = get_settings()
    base = {"base_domain": s.base_domain, "contact_email": s.contact_email, "sender_name": s.sender_name}
    return templates.get_template(name).render(**base, **context)


async def send(to: str, subject: str, html: str) -> bool:
    s = get_settings()
    if not s.smtp_host:
        log.warning("No SMTP host configured; not sending %r to %s", subject, to)
        return False
    msg = EmailMessage()
    msg["From"] = formataddr((s.mail_from_name, s.mail_from))
    msg["To"] = to
    msg["Subject"] = subject
    msg.set_content("This message requires an HTML capable mail client.")
    msg.add_alternative(html, subtype="html")
    try:
        await aiosmtplib.send(
            msg,
            hostname=s.smtp_host,
            port=s.smtp_port,
            username=s.smtp_user or None,
            password=s.smtp_password or None,
            start_tls=s.smtp_starttls,
        )
    except (aiosmtplib.SMTPException, OSError) as e:
        log.error("Sending mail to %s failed: %s", to, e)
        return False
    return True


async def send_confirmation(resource: Resource) -> bool:
    if not resource.email:
        return False
    html = render(
        "email-confirmation.html", url=resource.url, frequency=resource.frequency, resource_id=resource.id
    )
    return await send(resource.email, "BrAVA: Confirm your email", html)


async def send_report(
    resource: Resource, report: SuiteReport, report_id: str, previous: Sequence[TestReport] = ()
) -> bool:
    if not resource.email:
        return False
    fail_list = add_stats(report)
    col = report.collection
    passed = col.total - col.fails
    html = render(
        "email.html",
        url=resource.url,
        frequency=resource.frequency,
        resource_id=resource.id,
        report_id=report_id,
        test_name=col.name,
        passed=passed,
        total=col.total,
        fail_list=fail_list,
        previous=[{"id": r.id, "date": r.created_at.strftime("%Y-%m-%d %H:%M %Z").strip()} for r in previous],
    )
    subject = (
        f"[BrAVa - Report for scheduled BrAPI test] Passed tests: {passed}/{col.total} for {resource.url}"
    )
    return await send(resource.email, subject, html)
