import AppKit
let paths = CommandLine.arguments.dropFirst()
let size = NSSize(width: 1500, height: 900)
let result = NSImage(size: size)
result.lockFocus()
NSColor(srgbRed: 6/255, green: 24/255, blue: 49/255, alpha: 1).setFill()
NSRect(origin: .zero, size: size).fill()
for (i, path) in paths.enumerated() {
 guard let img = NSImage(contentsOfFile: path) else { continue }
 let box = NSRect(x: CGFloat(i) * 500, y: 100, width: 500, height: 700)
 let scale = min(box.width/img.size.width, box.height/img.size.height)
 let s = NSSize(width: img.size.width*scale, height: img.size.height*scale)
 img.draw(in: NSRect(x: box.midX-s.width/2, y: box.midY-s.height/2, width:s.width,height:s.height))
}
result.unlockFocus()
let rep = NSBitmapImageRep(data: result.tiffRepresentation!)!
try rep.representation(using: .png, properties: [:])!.write(to: URL(fileURLWithPath: "/tmp/wobbrik-asset-qa.png"))
