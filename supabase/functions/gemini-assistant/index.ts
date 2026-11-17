// Supabase Edge Function: gemini-assistant
// Mosque Seba (মসজিদ সেবা) - AI Assistant for Notices, Invitations, and Financial Audit Insights

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

interface RequestPayload {
  action: "draft_notice" | "draft_invitation" | "finance_audit" | "general_query";
  prompt: string;
  contextData?: Record<string, unknown>;
}

Deno.serve(async (req: Request) => {
  // Handle CORS Preflight
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const apiKey = Deno.env.get("GEMINI_API_KEY");
    if (!apiKey) {
      return new Response(
        JSON.stringify({ success: false, error: "GEMINI_API_KEY is not configured in Supabase Secrets." }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const body: RequestPayload = await req.json();
    const { action, prompt, contextData } = body;

    let systemInstruction = "আপনি মসজিদ সেবা (Mosque Seba) অ্যাপের বিশ্বস্ত এআই সহকারী। সবসময় ইসলামিক আদব রক্ষা করে প্রমিত বাংলায় উত্তর দিন।";

    if (action === "draft_notice") {
      systemInstruction = `আপনি একটি বাংলাদেশি মসজিদের অফিসিয়াল নোটিশ ড্রাফটার। 
ব্যবহারকারীর নির্দেশনার আলোকে একটি গম্ভীর ও স্পষ্ট অফিসিয়াল নোটিশ প্রস্তুত করুন। 
JSON ফরম্যাটে উত্তর দিন:
{
  "title": "নোটিশের স্পষ্ট শিরোনাম",
  "content": "পূর্ণাঙ্গ নোটিশের বিবরণ (বিসমিল্লাহ, বিস্তারিত বয়ান, সময়সূচি, কমিটি ও আমন্ত্রক)"
}`;
    } else if (action === "draft_invitation") {
      systemInstruction = `আপনি একটি বাংলাদেশি মসজিদের ইসলামি দাওয়াতনামা ও মাহফিল পোস্টার ড্রাফটার। 
ওয়াজ মাহফিল, ইফতার মাহফিল বা দোয়া অনুষ্ঠানের জন্য শালীন, হৃদয়গ্রাহী ও আকর্ষণীয় দাওয়াতনামা প্রস্তুত করুন। 
JSON ফরম্যাটে উত্তর দিন:
{
  "title": "মাহফিলের মূল শিরোনাম",
  "description": "বিস্তারিত দ্বীনি আহ্বান ও বয়ানের বিষয়বস্তু",
  "openingSalam": "নাহমাদুহু ওয়া নুসাল্লি আলা রাসুলিহিল কারিম... আসসালামু আলাইকুম ওয়া রাহমাতুল্লাহ"
}`;
    } else if (action === "finance_audit") {
      systemInstruction = `আপনি একটি মসজিদের স্বচ্ছ বায়তুলমাল আর্থিক নিরীক্ষক ও উপদেষ্টা। 
প্রদত্ত আয়, ব্যয়, নিট স্থিতি ও জুমার দান সম্পর্কিত তথ্যের ভিত্তিতে ৩-৪ লাইনের একটি দায়িত্বশীল, ইতিবাচক ও স্বচ্ছ অডিট সারসংক্ষেপ বাংলায় প্রদান করুন। 
কোনো অমিল থাকলে সতর্ক করুন এবং স্বচ্ছতার প্রশংসা করুন।`;
    }

    // Call Google Gemini API (gemini-2.5-flash is current production model)
    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`;

    const geminiRequest = {
      contents: [
        {
          role: "user",
          parts: [
            { text: `System Instruction: ${systemInstruction}\n\nUser Prompt: ${prompt}\n\nContext Data: ${JSON.stringify(contextData || {})}` }
          ]
        }
      ],
      generationConfig: {
        temperature: 0.3,
        maxOutputTokens: 1024,
      }
    };

    const response = await fetch(geminiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(geminiRequest)
    });

    const data = await response.json();

    if (!response.ok) {
      // Fallback to gemini-flash-latest
      const fallbackUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=${apiKey}`;
      const fallbackRes = await fetch(fallbackUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(geminiRequest)
      });
      const fallbackData = await fallbackRes.json();

      if (!fallbackRes.ok) {
        return new Response(
          JSON.stringify({ success: false, error: fallbackData.error?.message || "Gemini API request failed." }),
          { status: 502, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      const replyText = fallbackData.candidates?.[0]?.content?.parts?.[0]?.text || "";
      return new Response(
        JSON.stringify({ success: true, text: replyText }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const replyText = data.candidates?.[0]?.content?.parts?.[0]?.text || "";
    return new Response(
      JSON.stringify({ success: true, text: replyText }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );

  } catch (error) {
    return new Response(
      JSON.stringify({ success: false, error: (error as Error).message }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
