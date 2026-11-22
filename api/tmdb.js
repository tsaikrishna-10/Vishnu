
// Vercel Serverless Function: keeps TMDB credentials on the server.
module.exports = async function handler(req, res) {
  res.setHeader("Cache-Control", "s-maxage=300, stale-while-revalidate=600");

  if (req.method !== "GET") {
    res.setHeader("Allow", "GET");
    return res.status(405).json({ error: "Method not allowed" });
  }

  const token = process.env.TMDB_API_READ_ACCESS_TOKEN;
  const apiKey = process.env.TMDB_API_KEY;

  if (!token && !apiKey) {
    return res.status(503).json({ error: "TMDB is not configured on the VISHNU server." });
  }

  const rawPath = typeof req.query.path === "string" ? req.query.path : "";

  if (
    !rawPath.startsWith("/") ||
    rawPath.startsWith("//") ||
    rawPath.includes("\\") ||
    rawPath.includes("..") ||
    /[\r\n]/.test(rawPath)
  ) {
    return res.status(400).json({ error: "Invalid TMDB path" });
  }

  const upstream = new URL("https://api.themoviedb.org/3" + rawPath);

  const headers = { accept: "application/json" };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  } else {
    upstream.searchParams.set("api_key", apiKey);
  }

  try {
    const response = await fetch(upstream.toString(), { headers });
    const body = await response.text();

    res.status(response.status);
    res.setHeader(
      "Content-Type",
      response.headers.get("content-type") || "application/json"
    );

    return res.send(body);
  } catch (error) {
    console.error("VISHNU TMDB proxy error:", error);
    return res.status(502).json({
      error: "Could not reach TMDB. Please try again."
    });
  }
};
      
