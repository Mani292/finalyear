from playwright.sync_api import sync_playwright

def run_verification(page):
    # Navigate to overview
    page.goto("http://localhost:3000")
    page.wait_for_timeout(1000)

    # Click live traffic link
    page.get_by_role("link", name="Live Traffic").click()
    page.wait_for_timeout(1000)

    # Click forecast link
    page.get_by_role("link", name="Forecast").click()
    page.wait_for_timeout(1000)

    # Click congestion map link
    page.get_by_role("link", name="Congestion Map").click()
    page.wait_for_timeout(2000)

    # Click future routing link
    page.get_by_role("link", name="Future Routing").click()
    page.wait_for_timeout(1000)
    page.get_by_role("button", name="Find Best Future Route").click()
    page.wait_for_timeout(2000)

    # Click model benchmarks link
    page.get_by_role("link", name="Model Benchmarks").click()
    page.wait_for_timeout(1000)

    page.screenshot(path="/home/jules/verification/screenshots/trafficsense_dashboard.png")
    page.wait_for_timeout(1000)

if __name__ == "__main__":
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            record_video_dir="/home/jules/verification/videos"
        )
        page = context.new_page()
        try:
            run_verification(page)
        finally:
            context.close()
            browser.close()
